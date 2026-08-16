package com.example.unyieldingmight.Models

enum class ActivityMultiplier(val multiplier: Float) {
    INACTIVE(1.2f),
    LIGHT(1.375f),
    MODERATE(1.55f),
    HEAVY(1.725f),
    EXTREME(1.9f);

    companion object {
        fun valueOf(value: Float): ActivityMultiplier {
            return entries.find { it.multiplier == value } ?: MODERATE
        }
    }
}
