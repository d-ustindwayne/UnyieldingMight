package com.example.unyieldingmight;

import java.util.Date;

public class GymClass {
    public enum Intensity { BEGINNER, INTERMEDIATE, ATHLETE }
    private int ID;
    private String name;
    private Trainer trainer;
    private Intensity intensity;
    private Date startDateTime;
    private Date endDateTime;
    private String description;
    private int currentCapacity;
    private int maxCapacity;
    private ClassStatus status;
    private float avgCaloriesBurnedPerDay;

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
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

    public Intensity getIntensity() {
        return getIntensity(2000f);
    }

    public Intensity getIntensity(float userTdee) {
        float effectiveTdee = userTdee <= 0 ? 2000f : userTdee;
        float ratio = avgCaloriesBurnedPerDay / effectiveTdee;

        if (ratio <= 0.15f) {
            return Intensity.BEGINNER;
        } else if (ratio <= 0.25f) {
            return Intensity.INTERMEDIATE;
        } else {
            return Intensity.ATHLETE;
        }
    }

    public ClassStatus getStatus() {
        return status;
    }

    public float getAvgCaloriesBurnedPerDay() {
        return avgCaloriesBurnedPerDay;
    }

    GymClass(Builder builder) {
        this.ID = builder.ID;
        this.name = builder.name;
        this.trainer = builder.trainer;
        this.startDateTime = builder.startDateTime;
        this.endDateTime = builder.endDateTime;
        this.description = builder.description;
        this.currentCapacity = builder.currentCapacity;
        this.maxCapacity = builder.maxCapacity;
        this.status = builder.status;
        this.avgCaloriesBurnedPerDay = builder.avgCaloriesBurnedPerDay;
    }

    public static class Builder {
        private int ID;
        private String name;
        private Trainer trainer;
        private Date startDateTime;
        private Date endDateTime;
        private String description;
        private int currentCapacity;
        private int maxCapacity;
        private ClassStatus status;
        private float avgCaloriesBurnedPerDay;

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
        public Builder startDateTime(Date startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }
        public Builder endDateTime(Date endDateTime) {
            this.endDateTime = endDateTime;
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
        public Builder avgCaloriesBurnedPerDay(float calories) {
            this.avgCaloriesBurnedPerDay = calories;
            return this;
        }

        public GymClass build() {
            return new GymClass(this);
        }
    }
}
