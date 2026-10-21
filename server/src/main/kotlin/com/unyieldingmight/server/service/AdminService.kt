package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.email.GymEvent
import com.unyieldingmight.server.email.NotificationCenter
import com.unyieldingmight.server.repo.deactivateTrainer
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
import com.unyieldingmight.shared.model.AdminUserDto
import com.unyieldingmight.shared.model.NewsletterRequest
import com.unyieldingmight.shared.model.NewsletterResult
import com.unyieldingmight.shared.model.Role
import com.unyieldingmight.shared.model.TrainerDto
import com.unyieldingmight.shared.model.TrainerUpsertRequest
import java.time.Duration
import java.time.Instant

class AdminService(
    private val db: Database,
    private val notifications: NotificationCenter,
) {
    /** Role is re-read from the database so a demoted admin loses access immediately, not when their token expires. */
    suspend fun isAdmin(userId: Int): Boolean = db.tx { findRole(userId) } == Role.ADMIN

    suspend fun stats(now: Instant = Instant.now()): AdminStatsDto = db.tx {
        val weekAgo = now.minus(Duration.ofDays(7))
        AdminStatsDto(
            totalUsers = count("SELECT COUNT(*) FROM user_account").toInt(),
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

    suspend fun users(search: String?): List<AdminUserDto> = db.tx { listUsers(search) }

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

    private fun validateTrainer(req: TrainerUpsertRequest) = validate(
        Validation.name(req.firstName, "First name"),
        Validation.name(req.lastName, "Last name"),
        Validation.required(req.bio, "Bio", 500),
        Validation.required(req.specialty, "Specialty", 50),
    )
}
