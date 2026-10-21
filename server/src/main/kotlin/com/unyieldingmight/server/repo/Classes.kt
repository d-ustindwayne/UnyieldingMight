package com.unyieldingmight.server.repo

import com.unyieldingmight.server.db.Tx
import com.unyieldingmight.server.db.enum
import com.unyieldingmight.server.db.epochMillis
import com.unyieldingmight.server.db.intOrNull
import com.unyieldingmight.shared.model.AdminBookingDto
import com.unyieldingmight.shared.model.BookingDto
import com.unyieldingmight.shared.model.BookingStatus
import com.unyieldingmight.shared.model.ClassCategory
import com.unyieldingmight.shared.model.ClassUpsertRequest
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.TrainerDto
import com.unyieldingmight.shared.model.TrainerUpsertRequest
import java.sql.ResultSet
import java.time.Instant

// ---------- Trainers ----------

private fun ResultSet.toTrainer(prefix: String = ""): TrainerDto = TrainerDto(
    id = getInt("${prefix}id"),
    firstName = getString("${prefix}first_name"),
    lastName = getString("${prefix}last_name"),
    bio = getString("${prefix}bio"),
    specialty = getString("${prefix}specialty"),
)

fun Tx.listTrainers(): List<TrainerDto> =
    query("SELECT * FROM trainer WHERE active = TRUE ORDER BY first_name, last_name") { it.toTrainer() }

fun Tx.findTrainer(id: Int): TrainerDto? =
    queryOne("SELECT * FROM trainer WHERE id = ? AND active = TRUE", id) { it.toTrainer() }

fun Tx.insertTrainer(req: TrainerUpsertRequest): Int = insert(
    "INSERT INTO trainer (first_name, last_name, bio, specialty, active) VALUES (?, ?, ?, ?, TRUE)",
    req.firstName.trim(), req.lastName.trim(), req.bio.trim(), req.specialty.trim(),
)

fun Tx.updateTrainer(id: Int, req: TrainerUpsertRequest): Int = update(
    "UPDATE trainer SET first_name = ?, last_name = ?, bio = ?, specialty = ? WHERE id = ? AND active = TRUE",
    req.firstName.trim(), req.lastName.trim(), req.bio.trim(), req.specialty.trim(), id,
)

/** Trainers are soft-deleted so past classes keep their history. */
fun Tx.deactivateTrainer(id: Int): Int = update("UPDATE trainer SET active = FALSE WHERE id = ?", id)

// ---------- Classes ----------

private const val CLASS_SELECT = """
    SELECT c.*, t.id AS t_id, t.first_name AS t_first_name, t.last_name AS t_last_name, t.bio AS t_bio, t.specialty AS t_specialty
    FROM gym_class c
    LEFT JOIN trainer t ON t.id = c.trainer_id
"""

private fun ResultSet.toGymClass(bookedByMe: Boolean = false): GymClassDto = GymClassDto(
    id = getInt("id"),
    name = getString("name"),
    description = getString("description"),
    category = enum<ClassCategory>("category"),
    trainer = intOrNull("t_id")?.let { toTrainer("t_") },
    startsAt = epochMillis("starts_at"),
    endsAt = epochMillis("ends_at"),
    maxCapacity = getInt("max_capacity"),
    bookedCount = getInt("booked_count"),
    avgCalories = getInt("avg_calories"),
    priceCents = getInt("price_cents"),
    status = enum("status"),
    bookedByMe = bookedByMe,
)

/** Classes in a time window, flagged with whether [viewerId] holds an active booking. */
fun Tx.listClasses(from: Instant, to: Instant?, viewerId: Int?): List<GymClassDto> {
    val booked = viewerId?.let { activeBookedClassIds(it) } ?: emptySet()
    val sql = if (to == null) "$CLASS_SELECT WHERE c.ends_at >= ? ORDER BY c.starts_at"
    else "$CLASS_SELECT WHERE c.ends_at >= ? AND c.starts_at < ? ORDER BY c.starts_at"
    val params = if (to == null) arrayOf<Any?>(from) else arrayOf<Any?>(from, to)
    return query(sql, *params) { it.toGymClass(bookedByMe = it.getInt("id") in booked) }
}

fun Tx.findClass(id: Int, viewerId: Int? = null): GymClassDto? {
    val booked = viewerId?.let { activeBookedClassIds(it) } ?: emptySet()
    return queryOne("$CLASS_SELECT WHERE c.id = ?", id) { it.toGymClass(bookedByMe = id in booked) }
}

fun Tx.insertClass(req: ClassUpsertRequest, now: Instant = Instant.now()): Int = insert(
    """
    INSERT INTO gym_class (name, description, category, trainer_id, starts_at, ends_at, max_capacity, booked_count, avg_calories, price_cents, status, created_at, updated_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 'SCHEDULED', ?, ?)
    """,
    req.name.trim(), req.description.trim(), req.category, req.trainerId,
    Instant.ofEpochMilli(req.startsAt), Instant.ofEpochMilli(req.endsAt),
    req.maxCapacity, req.avgCalories, req.priceCents, now, now,
)

fun Tx.updateClass(id: Int, req: ClassUpsertRequest): Int = update(
    """
    UPDATE gym_class SET name = ?, description = ?, category = ?, trainer_id = ?, starts_at = ?, ends_at = ?,
        max_capacity = ?, avg_calories = ?, price_cents = ?, updated_at = ?
    WHERE id = ?
    """,
    req.name.trim(), req.description.trim(), req.category, req.trainerId,
    Instant.ofEpochMilli(req.startsAt), Instant.ofEpochMilli(req.endsAt),
    req.maxCapacity, req.avgCalories, req.priceCents, Instant.now(), id,
)

fun Tx.setClassStatus(id: Int, status: com.unyieldingmight.shared.model.ClassStatus): Int =
    update("UPDATE gym_class SET status = ?, updated_at = ? WHERE id = ?", status, Instant.now(), id)

fun Tx.deleteClass(id: Int): Int = update("DELETE FROM gym_class WHERE id = ?", id)

/**
 * Atomically takes a spot. The WHERE clause makes over-booking impossible even with
 * concurrent requests, which the original read-then-write code could not guarantee.
 */
fun Tx.reserveSpot(classId: Int, now: Instant = Instant.now()): Boolean = update(
    "UPDATE gym_class SET booked_count = booked_count + 1 WHERE id = ? AND status = 'SCHEDULED' AND booked_count < max_capacity AND starts_at > ?",
    classId, now,
) == 1

fun Tx.releaseSpot(classId: Int): Int =
    update("UPDATE gym_class SET booked_count = booked_count - 1 WHERE id = ? AND booked_count > 0", classId)

/** Scheduled classes that have ended; returned so attendees can be emailed. */
fun Tx.findFinishedClassIds(now: Instant): List<Int> =
    query("SELECT id FROM gym_class WHERE status = 'SCHEDULED' AND ends_at < ?", now) { it.getInt(1) }

// ---------- Bookings ----------

fun Tx.activeBookedClassIds(userId: Int): Set<Int> =
    query("SELECT class_id FROM class_booking WHERE user_id = ? AND status = 'BOOKED'", userId) { it.getInt(1) }.toSet()

data class BookingRow(val id: Int, val userId: Int, val classId: Int, val status: BookingStatus, val paymentId: Int?)

fun Tx.findBookingRow(userId: Int, classId: Int): BookingRow? =
    queryOne("SELECT * FROM class_booking WHERE user_id = ? AND class_id = ?", userId, classId) { it.toBookingRow() }

fun Tx.findBookingRowById(id: Int): BookingRow? =
    queryOne("SELECT * FROM class_booking WHERE id = ?", id) { it.toBookingRow() }

private fun ResultSet.toBookingRow() = BookingRow(getInt("id"), getInt("user_id"), getInt("class_id"), enum("status"), intOrNull("payment_id"))

/** Creates the booking, or re-activates a previously cancelled one (user_id + class_id is unique). */
fun Tx.upsertBooking(userId: Int, classId: Int, paymentId: Int?, now: Instant = Instant.now()): Int {
    val existing = findBookingRow(userId, classId)
    return if (existing == null) {
        insert(
            "INSERT INTO class_booking (user_id, class_id, status, payment_id, booked_at) VALUES (?, ?, 'BOOKED', ?, ?)",
            userId, classId, paymentId, now,
        )
    } else {
        update("UPDATE class_booking SET status = 'BOOKED', payment_id = ?, booked_at = ? WHERE id = ?", paymentId, now, existing.id)
        existing.id
    }
}

fun Tx.setBookingStatus(id: Int, status: BookingStatus): Int =
    update("UPDATE class_booking SET status = ? WHERE id = ?", status, id)

fun Tx.setBookingStatusForClass(classId: Int, from: BookingStatus, to: BookingStatus): Int =
    update("UPDATE class_booking SET status = ? WHERE class_id = ? AND status = ?", to, classId, from)

fun Tx.findBooking(id: Int): BookingDto? = listBookings("b.id = ?", id).firstOrNull()

fun Tx.listUserBookings(userId: Int): List<BookingDto> = listBookings("b.user_id = ?", userId)

private fun Tx.listBookings(where: String, param: Any): List<BookingDto> = query(
    """
    SELECT b.id AS b_id, b.status AS b_status, b.booked_at AS b_booked_at, COALESCE(p.amount_cents, 0) AS paid_cents,
           c.*, t.id AS t_id, t.first_name AS t_first_name, t.last_name AS t_last_name, t.bio AS t_bio, t.specialty AS t_specialty
    FROM class_booking b
    JOIN gym_class c ON c.id = b.class_id
    LEFT JOIN trainer t ON t.id = c.trainer_id
    LEFT JOIN payment p ON p.id = b.payment_id AND p.status = 'SUCCEEDED'
    WHERE $where
    ORDER BY c.starts_at DESC
    """,
    param,
) {
    val status = it.enum<BookingStatus>("b_status")
    BookingDto(
        id = it.getInt("b_id"),
        gymClass = it.toGymClass(bookedByMe = status == BookingStatus.BOOKED),
        status = status,
        bookedAt = it.epochMillis("b_booked_at"),
        paidCents = it.getInt("paid_cents"),
    )
}

fun Tx.listClassAttendees(classId: Int): List<AdminBookingDto> = query(
    """
    SELECT b.id, b.user_id, b.status, b.booked_at, u.first_name, u.last_name, u.email
    FROM class_booking b JOIN user_account u ON u.id = b.user_id
    WHERE b.class_id = ? ORDER BY b.booked_at
    """,
    classId,
) {
    AdminBookingDto(
        id = it.getInt("id"),
        userId = it.getInt("user_id"),
        userName = "${it.getString("first_name")} ${it.getString("last_name")}",
        userEmail = it.getString("email"),
        status = it.enum("status"),
        bookedAt = it.epochMillis("booked_at"),
    )
}

/** Booked users of a class with their payment, used for cancellation emails and refunds. */
data class Attendee(val bookingId: Int, val userId: Int, val email: String, val firstName: String, val paymentId: Int?)

fun Tx.listActiveAttendees(classId: Int): List<Attendee> = query(
    """
    SELECT b.id, b.user_id, b.payment_id, u.email, u.first_name
    FROM class_booking b JOIN user_account u ON u.id = b.user_id
    WHERE b.class_id = ? AND b.status = 'BOOKED'
    """,
    classId,
) { Attendee(it.getInt("id"), it.getInt("user_id"), it.getString("email"), it.getString("first_name"), it.intOrNull("payment_id")) }

/** Categories a user has booked before, for recommendations. */
fun Tx.favouriteCategories(userId: Int): Set<ClassCategory> = query(
    "SELECT DISTINCT c.category FROM class_booking b JOIN gym_class c ON c.id = b.class_id WHERE b.user_id = ? AND b.status IN ('BOOKED', 'ATTENDED')",
    userId,
) { it.enum<ClassCategory>("category") }.toSet()
