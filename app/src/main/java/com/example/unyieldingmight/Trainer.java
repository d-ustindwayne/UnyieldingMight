package com.example.unyieldingmight;

public class Trainer {
    private final int trainerId;
    private final String password;
    private final String description;

    Trainer(Builder builder){
        this.trainerId = builder.trainerId;
        this.password = null;
        this.description = builder.description;
    }

    public int getTrainerId (){
        return trainerId;
    }

    public String getPassword() {
        return password;
    }

    public String getDescription() {
        return description;
    }

    public static class Builder {
        private int trainerId;
        private String password;
        private String description;


        public Builder trainerId(int trainerId) {
            this.trainerId = trainerId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }
    }
}
