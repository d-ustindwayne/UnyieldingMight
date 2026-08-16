package com.example.unyieldingmight;

import com.example.unyieldingmight.Models.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserTest {
    public User user;

    @Before
    public void setup(){
        user = new User(1, "Dara", "123", "example@unyiedingmight.com", User.Role.ADMIN);
    }

    //Constructor testing
    @Test
    public void UserTestSuccess(){
        Assert.assertEquals(1, user.getId());
        Assert.assertEquals("Dara", user.getUsername());
        Assert.assertEquals("123", user.getPassword());
        Assert.assertEquals("example@unyiedingmight.com", user.getEmail());
        Assert.assertEquals(User.Role.ADMIN, user.getUserClass());
    }

    @Test
    public void getAdminUserTest(){
        User admin = new User(2, "Admin2", "admin123", "admin@unyieldingmight.com", User.Role.ADMIN);
        Assert.assertTrue(admin.isAdmin());
    }

    @Test
    public void getCustomerUserTest(){
        User customer = new User(3, "Customer", "customer123", "customer@unyieldingmight.com", User.Role.CUSTOMER);
        Assert.assertTrue(customer.isCustomer());
    }

}