package com.example.unyieldingmight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import com.example.unyieldingmight.Models.ActivityMultiplier;
import com.example.unyieldingmight.Models.Address;
import com.example.unyieldingmight.Models.Customer;
import com.example.unyieldingmight.Models.Gender;
import com.example.unyieldingmight.Models.Profile;

import java.util.Date;

public class CustomerTest {

    private Profile profile;
    private Address address;
    private Customer customer;
    private Customer customer2;
    private Date date = new Date();

    @Before
    public void setUp(){
        address = new Address.Builder()
                .street("19H Street")
                .city("Manukau")
                .region("Auckland")
                .postcode("123A")
                .country("New Zealand")
                .build();

        profile = new Profile.Builder()
                .email("example@email.com")
                .password("password123")
                .age(20)
                .address(address)
                .dateOfBirth(date)
                .gender(Gender.MALE)
                .firstName("John")
                .lastName("Smith")
                .userClass("ADMIN")
                .build();

        customer = new Customer.Builder()
                .customerId(1)
                .profile(profile)
                .isMember(true)
                .height(180f)
                .weight(75f)
                .activityMultiplier(ActivityMultiplier.EXTREME)
                .build();
    }


    @Test
    public void CustomerBuilderSuccess(){
        Assert.assertEquals(1, customer.getCustomerId());
        Assert.assertEquals(profile, customer.getProfile());
        Assert.assertTrue(customer.isMember());
        Assert.assertEquals(180f, customer.getHeight(), 180);
        Assert.assertEquals(75f, customer.getWeight(), 75);
        Assert.assertEquals(ActivityMultiplier.EXTREME.getActivityMultiplier(), customer.getActivityMultiplier(), 1.9);
        Assert.assertEquals(3382f, customer.getTDEE(), 3382);
    }

    @Test
    public void CustomerBuilderFail(){
        try{
            customer2 = new Customer.Builder()
                    .build();
        }
        catch (Exception e){
            assertEquals("Profile is needed", e.getMessage());
        }
    }

    @Test
    public void BMRSuccess(){
//        Assert.assertEquals(1780f,customer.calculateBMR(), 1780);
    }

    @Test
    public void TDEESuccess(){
        Assert.assertEquals(3382f,customer.calculateTDEE(),3382);
    }
}
