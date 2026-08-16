package com.example.unyieldingmight.Models

import java.sql.Date

data class GymClass(
    val iD: Int = 0,
    val name: String? = null,
    val trainer: Trainer? = null,
    val startDateTime: Date? = null,
    val endDateTime: Date? = null,
    val description: String? = null,
    val currentCapacity: Int = 0,
    val maxCapacity: Int = 0,
    val status: ClassStatus? = null,
    val avgCaloriesBurnedPerDay: Float = 0f
) {
    enum class Intensity {
        BEGINNER, INTERMEDIATE, ATHLETE
    }

    val intensity: Intensity
        get() = getIntensity(avgCaloriesBurnedPerDay)

    companion object {
        fun getIntensity(avgCalories: Float): Intensity {
            return when {
                avgCalories < 300 -> Intensity.BEGINNER
                avgCalories < 600 -> Intensity.INTERMEDIATE
                else -> Intensity.ATHLETE
            }
        }
    }
}
