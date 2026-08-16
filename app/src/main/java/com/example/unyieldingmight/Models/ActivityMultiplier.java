package com.example.unyieldingmight.Models;

public enum ActivityMultiplier {
    INACTIVE(1.2f),
    LIGHT(1.275f),
    MODERATE(1.55f),
    HEAVY(1.725f),
    EXTREME(1.9f);

    private final float multiplier;
    ActivityMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    public float getActivityMultiplier() {
        return multiplier;
    }
}
