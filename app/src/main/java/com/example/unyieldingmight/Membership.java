package com.example.unyieldingmight;

public class Membership {
    private int id;
    private String firstName;
    private String lastName;
    private float height;
    private float weight;
    private float activityMultiplier;
    private float tdee;

    public Membership(int id, String firstName, String lastName, float height, float weight, float activityMultiplier, float tdee) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.height = height;
        this.weight = weight;
        this.activityMultiplier = activityMultiplier;
        this.tdee = tdee;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public float getHeight() { return height; }
    public float getWeight() { return weight; }
    public float getActivityMultiplier() { return activityMultiplier; }
    public float getTdee() { return tdee; }
}
