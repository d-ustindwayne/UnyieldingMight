package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.repo.PaymentRecord
import com.unyieldingmight.server.repo.findMembershipDto
import com.unyieldingmight.server.repo.findMembershipPlan
import com.unyieldingmight.server.repo.findUserById
import com.unyieldingmight.server.repo.insertAddress
import com.unyieldingmight.server.repo.insertMembership
import com.unyieldingmight.server.repo.listMembershipPlans
import com.unyieldingmight.server.repo.renewMembership
import com.unyieldingmight.server.repo.setCustomerMembership
import com.unyieldingmight.server.repo.updateAddress
import com.unyieldingmight.server.repo.updateCustomerStats
import com.unyieldingmight.server.repo.updateUserNames
import com.unyieldingmight.server.validate
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.MembershipPlanDto
import com.unyieldingmight.shared.model.PaymentIntentDto
import com.unyieldingmight.shared.model.PaymentPurpose
import com.unyieldingmight.shared.model.PaymentResultDto
import com.unyieldingmight.shared.model.PaymentStatus
import com.unyieldingmight.shared.model.UpdateProfileRequest
import com.unyieldingmight.shared.model.UserDto
import java.time.Instant

/** Profile, body stats and membership for the signed-in user. */
class UserService(
    private val db: Database,
    private val payments: PaymentService,
) : PaymentService.Fulfiller {

    override val purpose = PaymentPurpose.MEMBERSHIP

    suspend fun me(userId: Int): UserDto = (db.tx { findUserById(userId) } ?: throw ApiException.unauthorized()).toDto()

    suspend fun update(userId: Int, req: UpdateProfileRequest): UserDto {
        validate(
            req.firstName?.let { Validation.name(it, "First name") },
            req.lastName?.let { Validation.name(it, "Last name") },
            req.address?.let { AuthService.validateAddress(it) },
            req.heightCm?.let { Validation.heightCm(it) },
            req.weightKg?.let { Validation.weightKg(it) },
        )
        db.tx {
            val user = findUserById(userId) ?: throw ApiException.unauthorized()
            var addressId = user.addressId
            req.address?.let { address ->
                if (addressId == null) addressId = insertAddress(address) else updateAddress(addressId!!, address)
            }
            updateUserNames(
                userId,
                req.firstName ?: user.firstName,
                req.lastName ?: user.lastName,
                addressId,
                req.newsletterOptIn ?: user.newsletterOptIn,
            )
            if (user.heightCm != null && (req.heightCm != null || req.weightKg != null || req.activityLevel != null)) {
                updateCustomerStats(
                    userId,
                    req.heightCm ?: user.heightCm,
                    req.weightKg ?: user.weightKg!!,
                    req.activityLevel ?: user.activityLevel!!,
                )
            }
        }
        return me(userId)
    }

    /** Links a gym-issued membership card to an existing account. */
    suspend fun linkMembership(userId: Int, membershipId: Int): UserDto {
        db.tx {
            val user = findUserById(userId) ?: throw ApiException.unauthorized()
            if (user.heightCm == null) throw ApiException.badRequest("Only customer accounts can hold a membership")
            if (user.toDto().isMember) throw ApiException.conflict("You already have an active membership")
            requireLinkableMembership(membershipId, user.email)
            setCustomerMembership(userId, membershipId)
        }
        return me(userId)
    }

    suspend fun plans(): List<MembershipPlanDto> = db.tx { listMembershipPlans() }

    suspend fun purchaseMembership(userId: Int, planId: Int): PaymentIntentDto {
        val (user, plan) = db.tx { findUserById(userId) to findMembershipPlan(planId) }
        if (user?.heightCm == null) throw ApiException.badRequest("Only customer accounts can buy a membership")
        if (plan == null) throw ApiException.notFound("Membership plan not found")
        return payments.start(userId, PaymentPurpose.MEMBERSHIP, plan.id, plan.priceCents, "${plan.name} membership")
    }

    override suspend fun fulfil(payment: PaymentRecord): PaymentResultDto {
        val membership = db.tx {
            val user = findUserById(payment.userId) ?: throw ApiException.notFound("Account not found")
            val plan = findMembershipPlan(payment.referenceId) ?: throw ApiException.notFound("Membership plan not found")
            val existing = user.membership
            val membershipId = if (existing != null) {
                renewMembership(existing.id, plan.id, plan.durationDays)
                existing.id
            } else {
                val now = Instant.now()
                val id = insertMembership(
                    email = user.email,
                    dateOfBirth = user.dateOfBirth,
                    gender = user.gender,
                    heightCm = user.heightCm!!,
                    weightKg = user.weightKg!!,
                    activityLevel = user.activityLevel!!,
                    addressId = user.addressId,
                    planId = plan.id,
                    startsAt = now,
                    expiresAt = now.plusSeconds(plan.durationDays * 86_400L),
                )
                setCustomerMembership(user.id, id)
                id
            }
            findMembershipDto(membershipId)!!
        }
        return PaymentResultDto(payment.id, PaymentStatus.SUCCEEDED, "Welcome to the club! Your membership is active.", membership = membership)
    }

    override suspend fun describe(payment: PaymentRecord) = PaymentResultDto(
        payment.id, PaymentStatus.SUCCEEDED, "Your membership is active.",
        membership = db.tx { findUserById(payment.userId)?.membership },
    )
}
