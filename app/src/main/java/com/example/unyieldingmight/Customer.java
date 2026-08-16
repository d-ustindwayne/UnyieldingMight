package com.example.unyieldingmight;

import androidx.annotation.NonNull;

import java.util.Date;

public class Customer implements Observer {
    private int customerId;
    private Profile profile;
    private boolean isMember;
    private float height;
    private float weight;
    private float activityMultiplier;
    private float TDEE;

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
        float weightComponent = 10 * weight;
        float heightComponent = 6.25f * height;
        float ageComponent = 5 * profile.getAge();

        switch (profile.getGender()) {
            case MALE:
                return weightComponent + heightComponent - ageComponent + 5;
            case FEMALE:
                return weightComponent + heightComponent - ageComponent - 161;
            default:
                float male = weightComponent + heightComponent - ageComponent + 5;
                float female = weightComponent + heightComponent - ageComponent - 161;
                return (male + female) / 2;
        }
    }
    public float calculateTDEE() {
        return this.calculateBMR() * activityMultiplier;
    }

    @Override
    public void update() {
        // Update class booked status by the customer and needed attributes
        // Or when a newsletter is uploaded by the admin
        // to be updated (user's class history | add count and append the class and its detail [this separate class])
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
            if (this.profile == null) { throw new IllegalStateException("Profile is needed");}
            return new Customer(this);
        }
    }
}