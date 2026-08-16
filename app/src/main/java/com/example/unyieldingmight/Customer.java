package com.example.unyieldingmight;

import androidx.annotation.NonNull;

import java.util.Date;

public class Customer {
    private final int customerId;
    private final Profile profile;
    private final boolean isMember;
    private final float height;
    private final float weight;
    private final float activityMultiplier;
    private final float TDEE;

    Customer(Builder builder){
        this.customerId = builder.customerId;
        this.profile = builder.profile;
        this.isMember = builder.isMember;
        this.height = builder.height;
        this.weight = builder.weight;
        this.activityMultiplier = builder.activityMultiplier;
        this.TDEE = calculateTDEE();
    }

    public int getCustomerId(){
        return customerId;
    }

    public boolean isMember() {
        return isMember;
    }

    public float getHeight() {
        return height;
    }

    public float getWeight() {
        return weight;
    }

    public float getActivityMultiplier() {
        return activityMultiplier;
    }

    public float getTDEE() {
        return TDEE;
    }

    public float calculateBMR() {
        if (profile.getGender().equals(Gender.MALE)) {
            return (float) ((10 * weight) + (6.25 * height) - (5 * profile.getAge()) + 5);
        } else if (profile.getGender().equals(Gender.FEMALE)) {
            return (float) ((10 * weight) + (6.25 * height) - (5 * profile.getAge()) - 161);
        } else {
            float TDEE1 = ((float) ((10 * weight) + (6.25 * height) - (5 * profile.getAge()) + 5));
            float TDEE2 = (float) ((10 * weight) + (6.25 * height) - (5 * profile.getAge()) - 161);
            return (float) ((TDEE1 + TDEE2) / 2);
        }
    }

    public float calculateTDEE() {
        return this.calculateBMR() * activityMultiplier;
    }

    // Builder class
    public static class Builder {
        private int customerId;
        private Profile profile;
        private boolean isMember;
        private float height;
        private float weight;
        private float BMR;
        private float activityMultiplier;
        private float TDEE;

        public Builder customerId(int customerId){
            this.customerId = customerId;
            return this;
        }

        public Builder profile(Profile profile) {
            this.profile = profile;
            return this;
        }

        public Builder isMember(boolean isMember){
            this.isMember = isMember;
            return this;
        }

        public Builder height(float height){
            this.height = height;
            return this;
        }

        public Builder weight(float weight){
            this.weight = weight;
            return this;
        }

        public Builder activityMultiplier(ActivityMultiplier multiplier) {
            this.activityMultiplier = multiplier.getActivityMultiplier();
            return this;
        }

        public Customer build(){
            return new Customer(this);
        }
    }
}