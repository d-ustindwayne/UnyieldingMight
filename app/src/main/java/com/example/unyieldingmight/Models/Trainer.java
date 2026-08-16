package com.example.unyieldingmight.Models;

public class Trainer {
    private int trainerID;
    private String firstName;
    private String lastName;
    private String profileDescription;

    public int getTrainerID() {
        return trainerID;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    Trainer(Builder builder) {
        this.trainerID = builder.trainerID;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.profileDescription = builder.profileDescription;
    }

    public static class Builder {
        private int trainerID;
        private String firstName;
        private String lastName;
        private String profileDescription;

        public Builder trainerID(int id) {
            this.trainerID = id;
            return this;
        }
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        public Builder profileDescription(String desc) {
            this.profileDescription = desc;
            return this;
        }

        public Trainer build() {
            return new Trainer(this);
        }
    }
}
