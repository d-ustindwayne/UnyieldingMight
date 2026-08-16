package com.example.unyieldingmight.Models

import java.sql.Date

data class GymBooking(
    val historyId: Int = 0,
    val gymClass: GymClass? = null,
    val status: ClassStatus? = null,
    val bookingDate: Date? = null
)
