package com.example.unyieldingmight.Models;

import java.util.Date;

public class Membership {
    private int id;
    private String email;
    private float height;
    private float weight;
    private float activityMultiplier;
    private float tdee;
    private Date dob;
    private String gender;
    private Integer addressId;
    private String street;
    private String city;
    private String region;
    private String postcode;
    private String country;

    public Membership(int id, String email, float height, float weight, float activityMultiplier, float tdee, Date dob, String gender) {
        this.id = id;
        this.email = email;
        this.height = height;
        this.weight = weight;
        this.activityMultiplier = activityMultiplier;
        this.tdee = tdee;
        this.dob = dob;
        this.gender = gender;
    }

    public void setAddress(Integer addressId, String street, String city, String region, String postcode, String country) {
        this.addressId = addressId;
        this.street = street;
        this.city = city;
        this.region = region;
        this.postcode = postcode;
        this.country = country;
    }

    public int getId() { return id; }
    public String getEmail() { return email; }
    public float getHeight() { return height; }
    public float getWeight() { return weight; }
    public float getActivityMultiplier() { return activityMultiplier; }
    public float getTdee() { return tdee; }
    public Date getDOB() { return dob; }
    public String getGender() { return gender; }
    public Integer getAddressId() { return addressId; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getRegion() { return region; }
    public String getPostcode() { return postcode; }
    public String getCountry() { return country; }

    @Override
    public String toString() {
        return "Membership{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", dob=" + dob +
                ", gender='" + gender + '\'' +
                ", addressId=" + addressId +
                '}';
    }
}
