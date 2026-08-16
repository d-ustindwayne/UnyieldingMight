package com.example.unyieldingmight

import com.example.unyieldingmight.Models.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CustomerTest {
    private lateinit var profile: Profile
    private lateinit var address: Address
    private lateinit var customer: Customer

    @Before
    fun setUp() {
        address = Address(
            street = "19H Street",
            city = "Manukau",
            region = "Auckland",
            postcode = "123A",
            country = "New Zealand"
        )

        profile = Profile(
            email = "example@email.com",
            password = "password123",
            age = 20,
            address = address,
            gender = Gender.MALE,
            firstName = "John",
            lastName = "Smith",
            userClass = "ADMIN"
        )

        customer = Customer(
            customerId = 1,
            profile = profile,
            isMember = true,
            height = 180f,
            weight = 75f,
            activityMultiplier = ActivityMultiplier.EXTREME.multiplier
        )
    }

    @Test
    fun testCustomerProperties() {
        Assert.assertEquals(1, customer.customerId)
        Assert.assertEquals(profile, customer.profile)
        Assert.assertTrue(customer.isMember)
        Assert.assertEquals(180f, customer.height)
        Assert.assertEquals(75f, customer.weight)
        Assert.assertEquals(ActivityMultiplier.EXTREME.multiplier, customer.activityMultiplier)
    }

    @Test
    fun testBMRCalculation() {
        val bmr = Customer.calculateBMR(
            customer.weight,
            customer.height,
            customer.profile.age,
            customer.profile.gender ?: Gender.OTHER
        )
        // BMR = (10 * 75) + (6.25 * 180) - (5 * 20) + 5 = 750 + 1125 - 100 + 5 = 1780
        Assert.assertEquals(1780f, bmr, 0.1f)
    }

    @Test
    fun testTDEECalculation() {
        // TDEE = BMR * multiplier = 1780 * 1.9 = 3382
        Assert.assertEquals(3382f, customer.calculateTDEE(), 0.1f)
    }
}
