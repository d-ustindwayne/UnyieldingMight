package com.example.unyieldingmight.services

import android.annotation.SuppressLint
import java.util.Random

class EmailOTP {
    val OTP: String

    var isAuthenticated: Boolean = false
        private set

    init {
        OTP = generateOTP()
    }

    fun validateOTP(input: String?): Boolean {
        isAuthenticated = OTP == input
        return isAuthenticated
    }

    @SuppressLint("DefaultLocale")
    private fun generateOTP(): String {
        val random = Random()
        val number = random.nextInt(999999)
        return String.format("%06d", number)
    }

    fun send(email: String) {
        val messageText = "Your verification code is: $OTP"
        val emailFunction = EmailFunction()
            .setReceiver(email, "User")
            .createEmail("Verification Code", messageText)
        emailFunction.sendEmail()
    }
}
