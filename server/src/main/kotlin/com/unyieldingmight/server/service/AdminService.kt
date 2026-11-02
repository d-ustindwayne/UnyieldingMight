package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.email.GymEvent
import com.unyieldingmight.server.email.NotificationCenter
import com.unyieldingmight.server.repo.StoredImage
import com.unyieldingmight.server.repo.deactivateTrainer
import com.unyieldingmight.server.repo.deleteUnusedImages
import com.unyieldingmight.server.repo.findImage
import com.unyieldingmight.server.repo.findUserByEmail
import com.unyieldingmight.server.repo.findUserById
import com.unyieldingmight.server.repo.insertAddress
import com.unyieldingmight.server.repo.insertImage
import com.unyieldingmight.server.repo.setActive
import com.unyieldingmight.server.repo.updateAccountDetails
import com.unyieldingmight.server.repo.updateAddress
import com.unyieldingmight.server.repo.updateCustomerStats
import com.unyieldingmight.server.repo.findRole
import com.unyieldingmight.server.repo.findTrainer
import com.unyieldingmight.server.repo.insertTrainer
import com.unyieldingmight.server.repo.listTrainers
import com.unyieldingmight.server.repo.listUsers
import com.unyieldingmight.server.repo.lowStockCount
import com.unyieldingmight.server.repo.newsletterRecipients
import com.unyieldingmight.server.repo.revenueCents
import com.unyieldingmight.server.repo.updateRole
import com.unyieldingmight.server.repo.updateTrainer
import com.unyieldingmight.server.validate
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.AdminStatsDto
import com.unyieldingmight.shared.model.AdminUpdateUserRequest
import com.unyieldingmight.shared.model.AdminUserDto
import com.unyieldingmight.shared.model.ImageUploadResponse
import com.unyieldingmight.shared.model.NewsletterRequest
import com.unyieldingmight.shared.model.NewsletterResult
import com.unyieldingmight.shared.model.Role
import com.unyieldingmight.shared.model.TrainerDto
import com.unyieldingmight.shared.model.TrainerUpsertRequest
import com.unyieldingmight.shared.model.UserDto
import java.time.Duration
import java.time.Instant

class AdminService(
    private val db: Database,
    private val notifications: NotificationCenter,
    private val classes: ClassService,
) {
    /** Role is re-read from the database so a demoted admin loses access immediately, not when their token expires. */
    suspend fun isAdmin(userId: Int): Boolean = db.tx { findRole(userId) } == Role.ADMIN

    suspend fun stats(now: Instant = Instant.now()): AdminStatsDto = db.tx {
        val weekAgo = now.minus(Duration.ofDays(7))
        AdminStatsDto(
            totalUsers = count("SELECT COUNT(*) FROM user_account WHERE active = TRUE").toInt(),
            activeMembers = count(
                """
                SELECT COUNT(*) FROM customer_profile cp JOIN membership m ON m.id = cp.membership_id
                WHERE m.status = 'ACTIVE' AND (m.expires_at IS NULL OR m.expires_at > ?)
                """,
                now,
            ).toInt(),
            upcomingClasses = count("SELECT COUNT(*) FROM gym_class WHERE status = 'SCHEDULED' AND starts_at > ?", now).toInt(),
            bookingsThisWeek = count("SELECT COUNT(*) FROM class_booking WHERE booked_at >= ? AND status <> 'CANCELLED'", weekAgo).toInt(),
            revenueCents = revenueCents(now.minus(Duration.ofDays(30))),
            lowStockItems = lowStockCount(),
        )
    }

    suspend fun users(search: String?, deactivated: Boolean = false): List<AdminUserDto> = db.tx { listUsers(search, deactivated) }

    suspend fun user(id: Int): UserDto = (db.tx { findUserById(id) } ?: throw ApiException.notFound("User not found")).toDto()

    suspend fun updateUser(id: Int, req: AdminUpdateUserRequest): UserDto {
        validate(
            Validation.email(req.email),
            Validation.name(req.firstName, "First name"),
            Validation.name(req.lastName, "Last name"),
            req.address?.let { AuthService.validateAddress(it) },
            req.heightCm?.let { Validation.heightCm(it) },
            req.weightKg?.let { Validation.weightKg(it) },
        )
        val dateOfBirth = AuthService.parseDateOfBirth(req.dateOfBirth)
        db.tx {
            val user = findUserById(id) ?: throw ApiException.notFound("User not found")
            val emailOwner = findUserByEmail(req.email)
            if (emailOwner != null && emailOwner.id != id) throw ApiException.conflict("Another account already uses this email", "email_taken")
            var addressId = user.addressId
            req.address?.let { address -> if (addressId == null) addressId = insertAddress(address) else updateAddress(addressId!!, address) }
            updateAccountDetails(id, req.email, req.firstName, req.lastName, dateOfBirth, req.gender, addressId)
            if (user.heightCm != null) {
                updateCustomerStats(id, req.heightCm ?: user.heightCm, req.weightKg ?: user.weightKg!!, req.activityLevel ?: user.activityLevel!!)
            }
        }
        return user(id)
    }

    /**
     * Deactivated accounts can't log in, and their open sessions stop working on the next request. History is kept,
     * but their upcoming class spots are released (and refunded) so other members can book them.
     */
    suspend fun setActive(actingAdminId: Int, userId: Int, active: Boolean): String {
        if (actingAdminId == userId && !active) throw ApiException.badRequest("You can't deactivate your own account")
        db.tx { if (setActive(userId, active) == 0) throw ApiException.notFound("User not found") }
        if (active) return "They can log in again."
        val (cancelled, refunded) = classes.releaseUpcomingBookings(userId)
        return when {
            cancelled == 0 -> "They can no longer log in. Their past bookings and orders are kept."
            refunded == 0 -> "They can no longer log in. $cancelled upcoming booking(s) cancelled."
            else -> "They can no longer log in. $cancelled upcoming booking(s) cancelled and $refunded refunded in full."
        }
    }

    suspend fun uploadImage(contentType: String, data: ByteArray): ImageUploadResponse {
        if (contentType !in IMAGE_TYPES) throw ApiException.badRequest("Photos must be JPEG, PNG or WebP")
        if (data.isEmpty() || data.size > MAX_IMAGE_BYTES) throw ApiException.badRequest("Photos must be smaller than 5 MB")
        val id = db.tx { insertImage(contentType, data) }
        return ImageUploadResponse("/images/uploads/$id")
    }

    /** Background job: removes photos that were uploaded but never saved, or have since been replaced. */
    suspend fun deleteUnusedImages(now: Instant = Instant.now()): Int = db.tx { deleteUnusedImages(now.minus(UNUSED_IMAGE_GRACE)) }

    suspend fun image(id: Int): StoredImage = db.tx { findImage(id) } ?: throw ApiException.notFound("Photo not found")

    suspend fun setRole(actingAdminId: Int, userId: Int, role: Role) {
        if (actingAdminId == userId && role != Role.ADMIN) throw ApiException.badRequest("You can't remove your own admin access")
        db.tx { if (updateRole(userId, role) == 0) throw ApiException.notFound("User not found") }
    }

    suspend fun trainers(): List<TrainerDto> = db.tx { listTrainers() }

    suspend fun createTrainer(req: TrainerUpsertRequest): TrainerDto {
        validateTrainer(req)
        return db.tx { findTrainer(insertTrainer(req))!! }
    }

    suspend fun updateTrainer(id: Int, req: TrainerUpsertRequest): TrainerDto {
        validateTrainer(req)
        return db.tx {
            if (updateTrainer(id, req) == 0) throw ApiException.notFound("Trainer not found")
            findTrainer(id)!!
        }
    }

    suspend fun deleteTrainer(id: Int) {
        db.tx { if (deactivateTrainer(id) == 0) throw ApiException.notFound("Trainer not found") }
    }

    /** Publishes a newsletter to every opted-in member through the notification observers. */
    suspend fun sendNewsletter(req: NewsletterRequest): NewsletterResult {
        validate(Validation.required(req.message, "Message", 5000))
        val recipients = db.tx { newsletterRecipients() }
        recipients.forEach { (email, name) ->
            notifications.publish(GymEvent.Newsletter(email, name, req.type, req.message, req.bulletPoints.filter { it.isNotBlank() }))
        }
        return NewsletterResult(recipients.size)
    }

    companion object {
        val IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/webp")
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
        val UNUSED_IMAGE_GRACE: Duration = Duration.ofDays(1)
    }

    private fun validateTrainer(req: TrainerUpsertRequest) = validate(
        Validation.name(req.firstName, "First name"),
        Validation.name(req.lastName, "Last name"),
        Validation.required(req.bio, "Bio", 500),
        Validation.required(req.specialty, "Specialty", 50),
    )
}
