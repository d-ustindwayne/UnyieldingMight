package com.example.unyieldingmight.Models;

public class Trainer {
    private int trainerID;
    private Profile profile;
    private String description;

    public Profile getProfile() {
        return profile;
    }

    public String getName() {
        if (profile != null) {
            return profile.getFirstName() + " " + profile.getLastName();
        }
        return "Unknown";
    }

    Trainer(Builder builder) {
        this.trainerID = builder.trainerID;
        this.profile = builder.profile;
        this.description = builder.description;
    }
    public static class Builder {
        private int trainerID;
        private Profile profile;
        private String description;
        public Builder trainerID(int id) {
            this.trainerID = id;
            return this;
        }
        public Builder profile(Profile profile) {
            this.profile = profile;
            return this;
        }
        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Trainer build() {
            return new Trainer(this);
        }
    }
}
