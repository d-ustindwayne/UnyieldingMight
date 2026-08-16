package com.example.unyieldingmight.Models

data class Address(
    val street: String? = null,
    val city: String? = null,
    val region: String? = null,
    val postcode: String? = null,
    val country: String? = null
) {
    override fun toString(): String {
        return listOfNotNull(street, city, region, postcode, country).joinToString(", ")
    }
}
