package com.example.unyieldingmight.Models;

public class Admin {
    private int adminID;
    private Profile profile;

    Admin(Builder builder) {
        this.adminID = builder.adminID;
        this.profile = builder.profile;
    }
    public static class Builder {
        private int adminID;
        private Profile profile;
        public Builder adminID(int id) {
            this.adminID = id;
            return this;
        }
        public Builder profile(Profile profile) {
            this.profile = profile;
            return this;
        }

        public Admin build() {
            return new Admin(this);
        }
    }
}
