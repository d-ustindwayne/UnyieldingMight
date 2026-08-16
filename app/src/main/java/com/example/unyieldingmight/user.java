package com.example.unyieldingmight;

public class user {
    String customerId;
    boolean isMember;
    double height;
    double weight;

    public user(String customerId, boolean isMember, double height, double weight) {
        this.customerId = customerId;
        this.isMember = isMember;
        this.height = height;
        this.weight = weight;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setMember(boolean member) {
        isMember = member;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
