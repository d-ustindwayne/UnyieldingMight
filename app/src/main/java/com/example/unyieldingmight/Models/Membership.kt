package com.example.unyieldingmight.Models

import java.sql.Date

data class Membership(
    val id: Int = 0,
    val email: String? = null,
    val height: Float = 0f,
    val weight: Float = 0f,
    val activityMultiplier: Float = 0f,
    val tdee: Float = 0f,
    val dOB: Date? = null,
    val gender: String? = null,
    var addressId: Int? = null,
    var street: String? = null,
    var city: String? = null,
    var region: String? = null,
    var postcode: String? = null,
    var country: String? = null
) {
    fun setAddress(
        addressId: Int?,
        street: String?,
        city: String?,
        region: String?,
        postcode: String?,
        country: String?
    ) {
        this.addressId = addressId
        this.street = street
        this.city = city
        this.region = region
        this.postcode = postcode
        this.country = country
    }

    override fun toString(): String {
        return "$street, $city, $region $postcode $country"
    }
}
