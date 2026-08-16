package com.example.unyieldingmight.models

import com.example.unyieldingmight.services.Database
import com.example.unyieldingmight.services.EmailNewsletter

data class Customer(
    val customerID: Int = 0,
    val profile: Profile,
    val isMember: Boolean = false,
    val height: Float = 0f,
    val weight: Float = 0f,
    val activityMultiplier: Float = 1.2f,
    val TDEE: Float = 0f
) : Observer {

    fun calculateTDEE(): Float {
        val BMR = calculateBMR(
            weight,
            height,
            profile.age,
            profile.gender ?: Gender.OTHER
        )
        return BMR * activityMultiplier
    }

    override fun update() {
        val subscribers = Database.getNewsletterSubscribers()
        val updateType = subscribers.latestUpdateType

        // Example logic for newsletter update
        val newsletter = EmailNewsletter(
            updateType ?: NewsletterType.INFORMATIONAL,
            "Hello ${profile.firstName},"
        )

        // The original code had some unfinished logic here, 
        // I'll keep the structure but make it compile.
        // newsletter.update() 
    }

    companion object {
        fun calculateBMR(weight: Float, height: Float, age: Int, gender: Gender): Float {
            return if (gender == Gender.MALE) {
                (10 * weight) + (6.25f * height) - (5 * age) + 5
            } else {
                (10 * weight) + (6.25f * height) - (5 * age) - 161
            }
        }
    }
}

