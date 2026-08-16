package com.example.unyieldingmight.models

import java.sql.Date

data class Membership(
    val ID: Int = 0,
    val email: String? = null,
    val height: Float = 0f,
    val weight: Float = 0f,
    val activityMultiplier: Float = 0f,
    val TDEE: Float = 0f,
    val DOB: Date? = null,
    val gender: String? = null,
    var addressID: Int? = null,
    var street: String? = null,
    var city: String? = null,
    var region: String? = null,
    var postcode: String? = null,
    var country: String? = null
) {
    fun setAddress(
        addressID: Int?,
        street: String?,
        city: String?,
        region: String?,
        postcode: String?,
        country: String?
    ) {
        this.addressID = addressID
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

