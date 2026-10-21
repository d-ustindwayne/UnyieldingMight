package com.unyieldingmight.shared.domain

import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.ClassCategory
import com.unyieldingmight.shared.model.ClassStatus
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.Intensity
import com.unyieldingmight.shared.model.RecommendationDto

/**
 * Rule-based class recommendations from a member's details.
 *
 * Each upcoming class is scored out of 100 from how well its intensity suits the person's
 * activity level, age and BMI, plus small bonuses for categories they already enjoy
 * and classes happening soon. Every point awarded comes with a human-readable reason.
 */
object ClassRecommender {

    data class Profile(
        val age: Int,
        val activityLevel: ActivityLevel,
        val bmi: Double,
        /** Categories of classes the person has booked before. */
        val favouriteCategories: Set<ClassCategory> = emptySet(),
    )

    fun preferredIntensity(profile: Profile): Intensity {
        var level = when (profile.activityLevel) {
            ActivityLevel.INACTIVE, ActivityLevel.LIGHT -> 0
            ActivityLevel.MODERATE -> 1
            ActivityLevel.HEAVY, ActivityLevel.EXTREME -> 2
        }
        // Ease off for older members and higher BMI where joint load matters more.
        if (profile.age >= 55) level -= 1
        if (profile.bmi >= 30) level -= 1
        return Intensity.entries[level.coerceIn(0, Intensity.entries.lastIndex)]
    }

    fun score(profile: Profile, gymClass: GymClassDto, now: Long): RecommendationDto? {
        if (gymClass.status != ClassStatus.SCHEDULED || gymClass.startsAt <= now) return null
        if (gymClass.isFull || gymClass.bookedByMe) return null

        val reasons = mutableListOf<String>()
        var score = 0

        val preferred = preferredIntensity(profile)
        when (kotlin.math.abs(preferred.ordinal - gymClass.intensity.ordinal)) {
            0 -> { score += 60; reasons += "Matches your ${preferred.name.lowercase()} intensity level" }
            1 -> { score += 30; reasons += if (gymClass.intensity.ordinal > preferred.ordinal) "A step up to challenge you" else "A lighter session for recovery" }
            else -> score += 0
        }

        if (gymClass.category in profile.favouriteCategories) {
            score += 15
            reasons += "You enjoy ${gymClass.category.label} classes"
        }

        if (profile.bmi >= 25 && gymClass.category in setOf(ClassCategory.CARDIO, ClassCategory.CONDITIONING, ClassCategory.DANCE)) {
            score += 10
            reasons += "Great for burning calories"
        }
        if (profile.age >= 55 && gymClass.category == ClassCategory.MIND_BODY) {
            score += 10
            reasons += "Supports mobility and balance"
        }

        val daysAway = (gymClass.startsAt - now) / 86_400_000.0
        if (daysAway <= 3) {
            score += 10
            reasons += "Coming up soon"
        }
        if (gymClass.spotsLeft <= 3) {
            score += 5
            reasons += "Only ${gymClass.spotsLeft} spot${if (gymClass.spotsLeft == 1) "" else "s"} left"
        }

        if (score == 0) return null
        return RecommendationDto(gymClass, score.coerceAtMost(100), reasons)
    }

    fun recommend(profile: Profile, classes: List<GymClassDto>, now: Long, limit: Int = 6): List<RecommendationDto> =
        classes.mapNotNull { score(profile, it, now) }
            .sortedWith(compareByDescending<RecommendationDto> { it.score }.thenBy { it.gymClass.startsAt })
            .take(limit)
}
