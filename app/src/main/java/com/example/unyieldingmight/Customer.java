package com.example.unyieldingmight;

public class Customer {
    private final int customerId;
    private final boolean isMember;
    private final double height;
    private final double weight;
    private final double activityMultiplier;
    private final double TDEE;

    Customer(Builder builder){
        this.customerId = builder.customerId;
        this.isMember = builder.isMember;
        this.height = builder.height;
        this.weight = builder.weight;
        this.activityMultiplier = builder.activityMultiplier;
        this.TDEE = builder.TDEE;
    }

    public int getCustomerId(){
        return customerId;
    }

    public boolean isMember() {
        return isMember;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public double getActivityMultiplier() {
        return activityMultiplier;
    }

    public double getTDEE() {
        return TDEE;
    }

    // Builder class
    public static class Builder {
        private int customerId;
        private boolean isMember;
        private double height;
        private double weight;
        private double activityMultiplier;
        private double TDEE;

        public Builder customerId(int customerId){
            this.customerId = customerId;
            return this;
        }

        public Builder isMember(boolean isMember){
            this.isMember = isMember;
            return this;
        }

        public Builder height(double height){
            this.height = height;
            return this;
        }

        public Builder weight(double weight){
            this.weight = weight;
            return this;
        }

        public Builder activityMultiplier(double activityMultiplier){
            this.activityMultiplier = activityMultiplier;
            return this;
        }

        public Builder TDEE(double TDEE){
            this.TDEE = TDEE;
            return this;
        }

        public Customer build(){
            return new Customer(this);
        }
    }
}
