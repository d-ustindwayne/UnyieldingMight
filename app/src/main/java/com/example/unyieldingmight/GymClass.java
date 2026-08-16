package com.example.unyieldingmight;

import java.util.Date;

public class GymClass {
    private int ID;
    private String name;
    private Trainer trainer;
    private Date DateAndTime;
    private String description;
    private int currentCapacity;
    private int maxCapacity;
    private ClassStatus status;
    private float avgCaloriesBurnedADay;

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public Date getDateAndTime() {
        return DateAndTime;
    }

    public String getDescription() {
        return description;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public ClassStatus getStatus() {
        return status;
    }

    public float getAvgCaloriesBurnedADay() {
        return avgCaloriesBurnedADay;
    }

    GymClass(Builder builder) {
        this.ID = builder.ID;
        this.name = builder.name;
        this.trainer = builder.trainer;
        this.DateAndTime = builder.DateAndTime;
        this.description = builder.description;
        this.currentCapacity = builder.currentCapacity;
        this.maxCapacity = builder.maxCapacity;
        this.status = builder.status;
        this.avgCaloriesBurnedADay = builder.avgCaloriesBurnedADay;
    }

    public static class Builder {
        private int ID;
        private String name;
        private Trainer trainer;
        private Date DateAndTime;
        private String description;
        private int currentCapacity;
        private int maxCapacity;
        private ClassStatus status;
        private float avgCaloriesBurnedADay;

        public Builder ID(int ID) {
            this.ID = ID;
            return this;
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder trainer(Trainer trainer) {
            this.trainer = trainer;
            return this;
        }
        public Builder DateAndTime(Date DateAndTime) {
            this.DateAndTime = DateAndTime;
            return this;
        }
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        public Builder currentCapacity(int currentCapacity) {
            this.currentCapacity = currentCapacity;
            return this;
        }
        public Builder maxCapacity(int maxCapacity) {
            this.maxCapacity = maxCapacity;
            return this;
        }
        public Builder status(ClassStatus status) {
            this.status = status;
            return this;
        }
        public Builder avgCaloriesBurnedADay(float calories) {
            this.avgCaloriesBurnedADay = calories;
            return this;
        }

        public GymClass build() {
            return new GymClass(this);
        }
    }
}
