package com.unyieldingmight.shared.domain

import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.BodyStatsDto
import com.unyieldingmight.shared.model.Gender
import kotlin.math.roundToInt

/** Body metrics shared by the server (stored profiles) and the app (calculators). */
object Fitness {

    /** Mifflin-St Jeor basal metabolic rate in kcal/day. OTHER uses the midpoint of the two formulas. */
    fun bmr(weightKg: Double, heightCm: Double, age: Int, gender: Gender): Double {
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return when (gender) {
            Gender.MALE -> base + 5
            Gender.FEMALE -> base - 161
            Gender.OTHER -> base - 78
        }
    }

    fun tdee(bmr: Double, activityLevel: ActivityLevel): Double = bmr * activityLevel.multiplier

    fun bmi(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0) return 0.0
        val m = heightCm / 100
        return weightKg / (m * m)
    }

    fun bmiCategory(bmi: Double): String = when {
        bmi <= 0 -> "Unknown"
        bmi < 18.5 -> "Underweight"
        bmi < 25 -> "Healthy"
        bmi < 30 -> "Overweight"
        else -> "Obese"
    }

    fun bodyStats(heightCm: Double, weightKg: Double, activityLevel: ActivityLevel, age: Int, gender: Gender): BodyStatsDto {
        val bmr = bmr(weightKg, heightCm, age, gender)
        return BodyStatsDto(
            heightCm = heightCm,
            weightKg = weightKg,
            activityLevel = activityLevel,
            age = age,
            bmi = Units.round(bmi(weightKg, heightCm), 1),
            bmr = bmr.roundToInt(),
            tdee = tdee(bmr, activityLevel).roundToInt(),
        )
    }

    /** Daily calorie targets around maintenance (TDEE). */
    data class CalorieGoals(val cut: Int, val maintain: Int, val bulk: Int)

    fun calorieGoals(tdee: Int): CalorieGoals = CalorieGoals(cut = tdee - 500, maintain = tdee, bulk = tdee + 300)
}
