package com.example.unyieldingmight

import com.example.unyieldingmight.models.User
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UserTest {
    private lateinit var user: User

    @Before
    fun setup() {
        user = User(
            ID = 1,
            username = "Dara",
            password = "123",
            email = "example@unyiedingmight.com",
            userClass = User.Role.ADMIN
        )
    }

    @Test
    fun testUserProperties() {
        Assert.assertEquals(1, user.ID)
        Assert.assertEquals("Dara", user.username)
        Assert.assertEquals("123", user.password)
        Assert.assertEquals("example@unyiedingmight.com", user.email)
        Assert.assertEquals(User.Role.ADMIN, user.userClass)
    }

    @Test
    fun testAdminRole() {
        val admin = User(userClass = User.Role.ADMIN)
        Assert.assertTrue(admin.isAdmin)
        Assert.assertFalse(admin.isCustomer)
    }

    @Test
    fun testCustomerRole() {
        val customer = User(userClass = User.Role.CUSTOMER)
        Assert.assertTrue(customer.isCustomer)
        Assert.assertFalse(customer.isAdmin)
    }
}
