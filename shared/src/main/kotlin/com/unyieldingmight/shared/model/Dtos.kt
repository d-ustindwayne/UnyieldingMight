package com.unyieldingmight.shared.model

import kotlinx.serialization.Serializable

// Conventions: timestamps are epoch milliseconds (UTC), calendar dates are ISO "yyyy-MM-dd",
// money is integer cents in the configured currency.

@Serializable
data class ApiError(val code: String, val message: String)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class AppConfigDto(
    val currency: String,
    val paymentMode: PaymentMode,
    val stripePublishableKey: String? = null,
    val newsEnabled: Boolean,
)

@Serializable
data class AddressDto(
    val street: String,
    val city: String,
    val region: String,
    val postcode: String,
    val country: String,
) {
    override fun toString(): String = listOf(street, city, region, postcode, country)
        .filter { it.isNotBlank() }
        .joinToString(", ")
}

// ---------- Auth & registration ----------

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: UserDto)

@Serializable
data class EmailRequest(val email: String)

@Serializable
data class VerifyOtpRequest(val email: String, val code: String)

/** Proof that the email was verified; required to finish registration. */
@Serializable
data class RegistrationTicket(val registrationToken: String, val email: String)

@Serializable
data class MembershipLookupRequest(val registrationToken: String, val membershipId: Int)

/** Details held on a gym-issued membership card, used to prefill registration. */
@Serializable
data class MembershipPreview(
    val membershipId: Int,
    val email: String,
    val dateOfBirth: String,
    val gender: Gender,
    val heightCm: Double,
    val weightKg: Double,
    val activityLevel: ActivityLevel,
    val address: AddressDto?,
)

@Serializable
data class RegisterRequest(
    val registrationToken: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val gender: Gender,
    val address: AddressDto,
    val heightCm: Double,
    val weightKg: Double,
    val activityLevel: ActivityLevel,
    val membershipId: Int? = null,
)

@Serializable
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)

@Serializable
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

// ---------- Users ----------

@Serializable
data class BodyStatsDto(
    val heightCm: Double,
    val weightKg: Double,
    val activityLevel: ActivityLevel,
    val age: Int,
    val bmi: Double,
    val bmr: Int,
    val tdee: Int,
)

@Serializable
data class MembershipDto(
    val id: Int,
    val planName: String?,
    val status: MembershipStatus,
    val startsAt: Long,
    val expiresAt: Long?,
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: Role,
    val dateOfBirth: String,
    val gender: Gender,
    val address: AddressDto?,
    val newsletterOptIn: Boolean,
    val stats: BodyStatsDto?,
    val membership: MembershipDto?,
    val createdAt: Long,
) {
    val fullName: String get() = "$firstName $lastName"
    val isAdmin: Boolean get() = role == Role.ADMIN
    val isMember: Boolean get() = membership?.status == MembershipStatus.ACTIVE
}

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val address: AddressDto? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val activityLevel: ActivityLevel? = null,
    val newsletterOptIn: Boolean? = null,
)

@Serializable
data class LinkMembershipRequest(val membershipId: Int)

@Serializable
data class MembershipPlanDto(
    val id: Int,
    val code: String,
    val name: String,
    val description: String,
    val priceCents: Int,
    val durationDays: Int,
)

@Serializable
data class PurchaseMembershipRequest(val planId: Int)

// ---------- Trainers & classes ----------

@Serializable
data class TrainerDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val bio: String,
    val specialty: String,
) {
    val fullName: String get() = "$firstName $lastName"
}

@Serializable
data class TrainerUpsertRequest(
    val firstName: String,
    val lastName: String,
    val bio: String,
    val specialty: String,
)

@Serializable
data class GymClassDto(
    val id: Int,
    val name: String,
    val description: String,
    val category: ClassCategory,
    val trainer: TrainerDto?,
    val startsAt: Long,
    val endsAt: Long,
    val maxCapacity: Int,
    val bookedCount: Int,
    val avgCalories: Int,
    val priceCents: Int,
    val status: ClassStatus,
    val bookedByMe: Boolean = false,
) {
    val intensity: Intensity get() = Intensity.fromCalories(avgCalories)
    val spotsLeft: Int get() = (maxCapacity - bookedCount).coerceAtLeast(0)
    val isFull: Boolean get() = spotsLeft == 0
    val durationMinutes: Long get() = (endsAt - startsAt) / 60_000
}

@Serializable
data class ClassUpsertRequest(
    val name: String,
    val description: String,
    val category: ClassCategory,
    val trainerId: Int?,
    val startsAt: Long,
    val endsAt: Long,
    val maxCapacity: Int,
    val avgCalories: Int,
    val priceCents: Int,
)

@Serializable
data class RecommendationDto(
    val gymClass: GymClassDto,
    val score: Int,
    val reasons: List<String>,
)

@Serializable
data class BookingDto(
    val id: Int,
    val gymClass: GymClassDto,
    val status: BookingStatus,
    val bookedAt: Long,
    val paidCents: Int,
)

@Serializable
data class AdminBookingDto(
    val id: Int,
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val status: BookingStatus,
    val bookedAt: Long,
)

/** Result of asking to book a class: either booked immediately (members / free classes) or payment is required. */
@Serializable
data class BookClassResponse(
    val booking: BookingDto? = null,
    val payment: PaymentIntentDto? = null,
)

// ---------- Payments ----------

@Serializable
data class PaymentIntentDto(
    val paymentId: Int,
    val mode: PaymentMode,
    val amountCents: Int,
    val currency: String,
    val description: String,
    val clientSecret: String? = null,
    val publishableKey: String? = null,
)

@Serializable
data class PaymentResultDto(
    val paymentId: Int,
    val status: PaymentStatus,
    val message: String,
    val booking: BookingDto? = null,
    val order: OrderDto? = null,
    val membership: MembershipDto? = null,
)

// ---------- Shop ----------

@Serializable
data class ShopItemDto(
    val id: Int,
    val name: String,
    val description: String,
    val category: ShopCategory,
    val priceCents: Int,
    val stock: Int,
    val imageUrl: String?,
    val active: Boolean,
)

@Serializable
data class ShopItemUpsertRequest(
    val name: String,
    val description: String,
    val category: ShopCategory,
    val priceCents: Int,
    val stock: Int,
    val imageUrl: String?,
    val active: Boolean = true,
)

@Serializable
data class CartLine(val itemId: Int, val quantity: Int)

@Serializable
data class CheckoutRequest(val lines: List<CartLine>)

@Serializable
data class OrderLineDto(val itemId: Int, val name: String, val quantity: Int, val unitPriceCents: Int)

@Serializable
data class OrderDto(
    val id: Int,
    val status: OrderStatus,
    val totalCents: Int,
    val createdAt: Long,
    val lines: List<OrderLineDto>,
)

@Serializable
data class CheckoutResponse(val order: OrderDto, val payment: PaymentIntentDto)

// ---------- News ----------

@Serializable
data class NewsArticleDto(
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val source: String,
    val publishedAt: Long,
)

// ---------- Admin ----------

@Serializable
data class AdminStatsDto(
    val totalUsers: Int,
    val activeMembers: Int,
    val upcomingClasses: Int,
    val bookingsThisWeek: Int,
    val revenueCents: Long,
    val lowStockItems: Int,
)

@Serializable
data class AdminUserDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: Role,
    val isMember: Boolean,
    val createdAt: Long,
)

@Serializable
data class RoleUpdateRequest(val role: Role)

@Serializable
data class NewsletterRequest(
    val type: NewsletterType,
    val message: String,
    val bulletPoints: List<String> = emptyList(),
)

@Serializable
data class NewsletterResult(val recipients: Int)
