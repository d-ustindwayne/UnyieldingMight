package com.example.unyieldingmight.Models

import java.sql.Date

data class Profile(
    val email: String? = null,
    val password: String? = null,
    val age: Int = 0,
    val address: Address? = null,
    val dateOfBirth: Date? = null,
    val gender: Gender? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val userClass: String? = null
)
