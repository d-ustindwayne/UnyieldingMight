package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.email.GymEvent
import com.unyieldingmight.server.email.NotificationCenter
import com.unyieldingmight.server.repo.PaymentRecord
import com.unyieldingmight.server.repo.deleteClass
import com.unyieldingmight.server.repo.favouriteCategories
import com.unyieldingmight.server.repo.findBooking
import com.unyieldingmight.server.repo.findBookingRow
import com.unyieldingmight.server.repo.findBookingRowById
import com.unyieldingmight.server.repo.findClass
import com.unyieldingmight.server.repo.findFinishedClassIds
import com.unyieldingmight.server.repo.findPayment
import com.unyieldingmight.server.repo.findTrainer
import com.unyieldingmight.server.repo.findUserById
import com.unyieldingmight.server.repo.insertClass
import com.unyieldingmight.server.repo.listActiveAttendees
import com.unyieldingmight.server.repo.listClassAttendees
import com.unyieldingmight.server.repo.listClasses
import com.unyieldingmight.server.repo.listUserBookings
import com.unyieldingmight.server.repo.releaseSpot
import com.unyieldingmight.server.repo.reserveSpot
import com.unyieldingmight.server.repo.setBookingStatus
import com.unyieldingmight.server.repo.setBookingStatusForClass
import com.unyieldingmight.server.repo.setClassStatus
import com.unyieldingmight.server.repo.updateClass
import com.unyieldingmight.server.repo.upsertBooking
import com.unyieldingmight.server.validate
import com.unyieldingmight.shared.domain.ClassRecommender
import com.unyieldingmight.shared.domain.Fitness
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.AdminBookingDto
import com.unyieldingmight.shared.model.BookClassResponse
import com.unyieldingmight.shared.model.BookingDto
import com.unyieldingmight.shared.model.BookingStatus
import com.unyieldingmight.shared.model.ClassStatus
import com.unyieldingmight.shared.model.ClassUpsertRequest
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.MessageResponse
import com.unyieldingmight.shared.model.PaymentPurpose
import com.unyieldingmight.shared.model.PaymentResultDto
import com.unyieldingmight.shared.model.PaymentStatus
import com.unyieldingmight.shared.model.RecommendationDto
import java.time.Duration
import java.time.Instant

class ClassService(
    private val db: Database,
    private val payments: PaymentService,
    private val notifications: NotificationCenter,
) : PaymentService.Fulfiller {

    override val purpose = PaymentPurpose.CLASS_BOOKING

    // ---------- Browsing ----------

    suspend fun list(viewerId: Int?, from: Instant = Instant.now(), to: Instant? = null): List<GymClassDto> =
        db.tx { listClasses(from, to, viewerId) }

    suspend fun get(id: Int, viewerId: Int?): GymClassDto =
        db.tx { findClass(id, viewerId) } ?: throw ApiException.notFound("Class not found")

    suspend fun recommended(userId: Int, limit: Int = 6): List<RecommendationDto> {
        val (user, classes, favourites) = db.tx {
            Triple(findUserById(userId), listClasses(Instant.now(), null, userId), favouriteCategories(userId))
        }
        val height = user?.heightCm ?: return emptyList()
        val weight = user.weightKg ?: return emptyList()
        val level = user.activityLevel ?: return emptyList()
        val profile = ClassRecommender.Profile(
            age = user.age(),
            activityLevel = level,
            bmi = Fitness.bmi(weight, height),
            favouriteCategories = favourites,
        )
        return ClassRecommender.recommend(profile, classes, System.currentTimeMillis(), limit)
    }

    // ---------- Booking ----------

    /** Members and free classes are booked straight away; everyone else gets a payment to complete. */
    suspend fun book(userId: Int, classId: Int): BookClassResponse {
        val (user, gymClass, existing) = db.tx { Triple(findUserById(userId), findClass(classId, userId), findBookingRow(userId, classId)) }
        if (user == null) throw ApiException.unauthorized()
        if (gymClass == null) throw ApiException.notFound("Class not found")
        if (existing?.status == BookingStatus.BOOKED) throw ApiException.conflict("You've already booked this class", "already_booked")
        checkBookable(gymClass)

        if (user.toDto().isMember || gymClass.priceCents == 0) {
            val booking = db.tx {
                if (!reserveSpot(classId)) throw ApiException.conflict("Sorry, this class just filled up", "class_full")
                findBooking(upsertBooking(userId, classId, paymentId = null))!!
            }
            notifications.publish(GymEvent.BookingConfirmed(user.email, user.firstName, gymClass.name, Instant.ofEpochMilli(gymClass.startsAt)))
            return BookClassResponse(booking = booking)
        }

        val payment = payments.start(userId, PaymentPurpose.CLASS_BOOKING, classId, gymClass.priceCents, "Class: ${gymClass.name}")
        return BookClassResponse(payment = payment)
    }

    override suspend fun fulfil(payment: PaymentRecord): PaymentResultDto {
        val user = db.tx { findUserById(payment.userId) } ?: throw ApiException.notFound("Account not found")
        val booking = db.tx {
            val existing = findBookingRow(payment.userId, payment.referenceId)
            if (existing?.status == BookingStatus.BOOKED) throw ApiException.conflict("You'd already booked this class.")
            if (!reserveSpot(payment.referenceId)) throw ApiException.conflict("Sorry, this class filled up before your payment finished.")
            findBooking(upsertBooking(payment.userId, payment.referenceId, payment.id))!!
        }
        notifications.publish(
            GymEvent.BookingConfirmed(user.email, user.firstName, booking.gymClass.name, Instant.ofEpochMilli(booking.gymClass.startsAt))
        )
        return PaymentResultDto(payment.id, PaymentStatus.SUCCEEDED, "You're booked in for ${booking.gymClass.name}!", booking = booking)
    }

    override suspend fun describe(payment: PaymentRecord): PaymentResultDto {
        val booking = db.tx { findBookingRow(payment.userId, payment.referenceId)?.let { findBooking(it.id) } }
        return PaymentResultDto(payment.id, PaymentStatus.SUCCEEDED, "You're booked in!", booking = booking)
    }

    suspend fun myBookings(userId: Int): List<BookingDto> = db.tx { listUserBookings(userId) }

    /** Cancels a booking. Paid bookings cancelled at least [REFUND_WINDOW] before the class are refunded. */
    suspend fun cancelBooking(userId: Int, bookingId: Int): MessageResponse {
        val (row, gymClass) = db.tx {
            val row = findBookingRowById(bookingId)
            row to row?.let { findClass(it.classId) }
        }
        if (row == null || row.userId != userId || gymClass == null) throw ApiException.notFound("Booking not found")
        if (row.status != BookingStatus.BOOKED) throw ApiException.conflict("This booking isn't active")
        if (gymClass.startsAt <= System.currentTimeMillis()) throw ApiException.badRequest("This class has already started")

        db.tx {
            setBookingStatus(row.id, BookingStatus.CANCELLED)
            releaseSpot(row.classId)
        }

        val payment = row.paymentId?.let { db.tx { findPayment(it) } }
        if (payment == null || payment.status != PaymentStatus.SUCCEEDED) return MessageResponse("Your booking has been cancelled.")
        val refundable = Instant.ofEpochMilli(gymClass.startsAt).minus(REFUND_WINDOW).isAfter(Instant.now())
        return if (refundable && payments.refund(payment)) {
            MessageResponse("Your booking has been cancelled and your payment refunded.")
        } else {
            MessageResponse("Your booking has been cancelled. Cancellations within 24 hours of the class aren't refunded.")
        }
    }

    // ---------- Admin ----------

    suspend fun create(req: ClassUpsertRequest): GymClassDto {
        validateClass(req)
        val id = db.tx {
            req.trainerId?.let { findTrainer(it) ?: throw ApiException.badRequest("Trainer not found") }
            insertClass(req)
        }
        return get(id, null)
    }

    suspend fun update(id: Int, req: ClassUpsertRequest): GymClassDto {
        validateClass(req)
        db.tx {
            val current = findClass(id) ?: throw ApiException.notFound("Class not found")
            if (current.status != ClassStatus.SCHEDULED) throw ApiException.conflict("Only scheduled classes can be edited")
            if (req.maxCapacity < current.bookedCount) {
                throw ApiException.badRequest("Capacity can't be lower than the ${current.bookedCount} people already booked", "validation")
            }
            req.trainerId?.let { findTrainer(it) ?: throw ApiException.badRequest("Trainer not found") }
            updateClass(id, req)
        }
        return get(id, null)
    }

    /** Cancels a class, refunds paid bookings and lets every attendee know. */
    suspend fun cancelClass(id: Int): MessageResponse {
        val (gymClass, attendees) = db.tx {
            val gymClass = findClass(id) ?: throw ApiException.notFound("Class not found")
            if (gymClass.status != ClassStatus.SCHEDULED) throw ApiException.conflict("This class isn't scheduled")
            val attendees = listActiveAttendees(id)
            setClassStatus(id, ClassStatus.CANCELLED)
            setBookingStatusForClass(id, BookingStatus.BOOKED, BookingStatus.CLASS_CANCELLED)
            gymClass to attendees
        }
        var refunds = 0
        for (attendee in attendees) {
            val payment = attendee.paymentId?.let { db.tx { findPayment(it) } }
            val refunded = payment?.status == PaymentStatus.SUCCEEDED && payments.refund(payment)
            if (refunded) refunds++
            notifications.publish(
                GymEvent.ClassCancelled(attendee.email, attendee.firstName, gymClass.name, Instant.ofEpochMilli(gymClass.startsAt), refunded)
            )
        }
        return MessageResponse("Class cancelled. ${attendees.size} attendee(s) notified, $refunds refund(s) issued.")
    }

    /** Deleting is only allowed while nobody is booked; otherwise cancel instead. */
    suspend fun delete(id: Int) {
        db.tx {
            val gymClass = findClass(id) ?: throw ApiException.notFound("Class not found")
            if (gymClass.status == ClassStatus.SCHEDULED && gymClass.bookedCount > 0) {
                throw ApiException.conflict("People are booked into this class. Cancel it instead so they're notified.", "has_bookings")
            }
            deleteClass(id)
        }
    }

    suspend fun attendees(id: Int): List<AdminBookingDto> = db.tx {
        findClass(id) ?: throw ApiException.notFound("Class not found")
        listClassAttendees(id)
    }

    /** Background job: completes classes that have ended and thanks the people who attended. */
    suspend fun completeFinishedClasses(now: Instant = Instant.now()): Int {
        val finished = db.tx { findFinishedClassIds(now) }
        for (id in finished) {
            val (gymClass, attendees) = db.tx {
                val attendees = listActiveAttendees(id)
                setClassStatus(id, ClassStatus.COMPLETED)
                setBookingStatusForClass(id, BookingStatus.BOOKED, BookingStatus.ATTENDED)
                findClass(id) to attendees
            }
            attendees.forEach { notifications.publish(GymEvent.ClassFinished(it.email, it.firstName, gymClass?.name ?: "your class")) }
        }
        return finished.size
    }

    private fun checkBookable(gymClass: GymClassDto) {
        when {
            gymClass.status == ClassStatus.CANCELLED -> throw ApiException.conflict("This class has been cancelled", "class_cancelled")
            gymClass.status == ClassStatus.COMPLETED || gymClass.startsAt <= System.currentTimeMillis() ->
                throw ApiException.conflict("This class has already started", "class_started")
            gymClass.isFull -> throw ApiException.conflict("Sorry, this class is full", "class_full")
        }
    }

    private fun validateClass(req: ClassUpsertRequest) {
        validate(
            Validation.required(req.name, "Class name"),
            Validation.required(req.description, "Description", 1000),
            if (req.endsAt <= req.startsAt) "The class must end after it starts" else null,
            if (req.endsAt - req.startsAt > Duration.ofHours(6).toMillis()) "Classes can't be longer than 6 hours" else null,
            if (req.maxCapacity !in 1..500) "Capacity must be between 1 and 500" else null,
            if (req.avgCalories !in 0..3000) "Average calories must be between 0 and 3000" else null,
            if (req.priceCents !in 0..100_000) "Price must be between $0 and $1000" else null,
        )
    }

    companion object {
        val REFUND_WINDOW: Duration = Duration.ofHours(24)
    }
}
