package com.unyieldingmight.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role { ADMIN, CUSTOMER }

@Serializable
enum class Gender { MALE, FEMALE, OTHER }

/** Harris-Benedict style activity multipliers used to turn BMR into TDEE. */
@Serializable
enum class ActivityLevel(val multiplier: Double, val label: String, val description: String) {
    INACTIVE(1.2, "Inactive", "Little or no exercise"),
    LIGHT(1.375, "Light", "Exercise 1–3 days a week"),
    MODERATE(1.55, "Moderate", "Exercise 3–5 days a week"),
    HEAVY(1.725, "Heavy", "Exercise 6–7 days a week"),
    EXTREME(1.9, "Extreme", "Hard training twice a day or a physical job");

    companion object {
        /** Maps a legacy stored multiplier (e.g. 1.55) back to its level. */
        fun fromMultiplier(value: Double): ActivityLevel =
            entries.minBy { kotlin.math.abs(it.multiplier - value) }
    }
}

@Serializable
enum class Intensity {
    BEGINNER, INTERMEDIATE, ATHLETE;

    companion object {
        fun fromCalories(avgCalories: Int): Intensity = when {
            avgCalories < 300 -> BEGINNER
            avgCalories < 600 -> INTERMEDIATE
            else -> ATHLETE
        }
    }
}

@Serializable
enum class ClassCategory(val label: String) {
    STRENGTH("Strength"),
    CARDIO("Cardio"),
    COMBAT("Combat"),
    MIND_BODY("Mind & Body"),
    DANCE("Dance"),
    CONDITIONING("Conditioning"),
}

@Serializable
enum class ClassStatus { SCHEDULED, CANCELLED, COMPLETED }

@Serializable
enum class BookingStatus { BOOKED, CANCELLED, ATTENDED, CLASS_CANCELLED }

@Serializable
enum class MembershipStatus { ACTIVE, EXPIRED, CANCELLED }

@Serializable
enum class ShopCategory(val label: String) {
    APPAREL("Apparel"),
    SUPPLEMENTS("Supplements"),
    EQUIPMENT("Equipment"),
    ACCESSORIES("Accessories"),
}

@Serializable
enum class OrderStatus { PENDING, PAID, CANCELLED }

@Serializable
enum class PaymentPurpose { CLASS_BOOKING, SHOP_ORDER, MEMBERSHIP }

@Serializable
enum class PaymentStatus { PENDING, SUCCEEDED, FAILED, CANCELLED, REFUNDED }

/** STRIPE uses the Stripe PaymentSheet; MOCK is a simulated provider used when no Stripe key is configured. */
@Serializable
enum class PaymentMode { STRIPE, MOCK }

@Serializable
enum class NewsletterType {
    INFORMATIONAL,
    PROMOTIONAL,
    UPDATES,
    BOOK_CONFIRMED,
    CLASS_FINISHED,
    CLASS_CANCELLED,
}

@Serializable
enum class NewsTopic(val label: String) {
    FITNESS("Fitness"),
    HEALTH("Health"),
    NUTRITION("Nutrition"),
    GYM("Gym"),
}
