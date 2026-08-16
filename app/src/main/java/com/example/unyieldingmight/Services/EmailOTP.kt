package com.example.unyieldingmight.Services

import android.annotation.SuppressLint
import java.util.Random

class EmailOTP {
    val oTP: String

    var isAuthenticated: Boolean = false
        private set

    init {
        oTP = generateOTP()
    }

    fun validateOTP(input: String?): Boolean {
        isAuthenticated = oTP == input
        return isAuthenticated
    }

    @SuppressLint("DefaultLocale")
    private fun generateOTP(): String {
        val random = Random()
        val number = random.nextInt(999999)
        return String.format("%06d", number)
    }

    fun send(email: String) {
        val messageText = "Your verification code is: $oTP"
        val emailFunction = EmailFunction()
            .setReceiver(email, "User")
            .createEmail("Verification Code", messageText)
        emailFunction.sendEmail()
    }
}
