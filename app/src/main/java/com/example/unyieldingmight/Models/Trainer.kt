package com.example.unyieldingmight.Models

data class Trainer(
    val trainerID: Int = 0,
    val firstName: String? = null,
    val lastName: String? = null,
    val profileDescription: String? = null
) {
    val name: String
        get() = "$firstName $lastName"
}
