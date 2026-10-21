package com.unyieldingmight.shared.domain

import kotlin.math.roundToInt

/**
 * Estimates calories burned using MET values from the Compendium of Physical Activities.
 * kcal/min = MET × 3.5 × bodyweight(kg) / 200
 */
object CalorieBurner {

    data class Activity(val id: String, val name: String, val group: String, val met: Double)

    val activities: List<Activity> = listOf(
        Activity("walking", "Walking (brisk, 5.6 km/h)", "Cardio", 4.3),
        Activity("running_8", "Running (8 km/h)", "Cardio", 8.3),
        Activity("running_11", "Running (11 km/h)", "Cardio", 11.0),
        Activity("cycling_moderate", "Cycling (moderate, 19–22 km/h)", "Cardio", 8.0),
        Activity("spin", "Spin class", "Cardio", 8.5),
        Activity("rowing", "Rowing machine (moderate)", "Cardio", 7.0),
        Activity("elliptical", "Elliptical trainer", "Cardio", 5.0),
        Activity("swimming", "Swimming laps (moderate)", "Cardio", 5.8),
        Activity("jump_rope", "Jump rope", "Cardio", 11.8),
        Activity("hiit", "HIIT / circuit training", "Conditioning", 8.0),
        Activity("crossfit", "CrossFit-style WOD", "Conditioning", 9.0),
        Activity("weights_light", "Weight training (light effort)", "Strength", 3.5),
        Activity("weights_vigorous", "Weight training (vigorous)", "Strength", 6.0),
        Activity("powerlifting", "Powerlifting", "Strength", 6.0),
        Activity("bodyweight", "Calisthenics / bodyweight", "Strength", 3.8),
        Activity("boxing_bag", "Boxing (heavy bag)", "Combat", 5.5),
        Activity("boxing_sparring", "Boxing (sparring)", "Combat", 7.8),
        Activity("muay_thai", "Muay Thai / kickboxing", "Combat", 10.3),
        Activity("yoga_hatha", "Yoga (hatha)", "Mind & Body", 2.5),
        Activity("yoga_power", "Yoga (power/vinyasa)", "Mind & Body", 4.0),
        Activity("pilates", "Pilates", "Mind & Body", 3.0),
        Activity("stretching", "Stretching / mobility", "Mind & Body", 2.3),
        Activity("zumba", "Zumba / aerobic dance", "Dance", 6.5),
        Activity("rugby", "Rugby (competitive)", "Sport", 8.3),
        Activity("basketball", "Basketball (game)", "Sport", 6.5),
        Activity("football", "Football / soccer (casual)", "Sport", 7.0),
    )

    fun byId(id: String): Activity? = activities.firstOrNull { it.id == id }

    fun caloriesBurned(met: Double, weightKg: Double, minutes: Double): Int {
        if (met <= 0 || weightKg <= 0 || minutes <= 0) return 0
        return (met * 3.5 * weightKg / 200.0 * minutes).roundToInt()
    }

    /** Minutes of an activity needed to burn [targetKcal]. */
    fun minutesToBurn(met: Double, weightKg: Double, targetKcal: Int): Int {
        if (met <= 0 || weightKg <= 0 || targetKcal <= 0) return 0
        return (targetKcal / (met * 3.5 * weightKg / 200.0)).roundToInt()
    }
}
