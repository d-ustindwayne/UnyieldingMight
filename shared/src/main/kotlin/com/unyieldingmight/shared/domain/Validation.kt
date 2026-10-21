package com.unyieldingmight.shared.domain

/** Input rules enforced by the server and mirrored in the app for instant feedback. Each returns an error message or null. */
object Validation {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val POSTCODE = Regex("^[A-Za-z0-9 -]{3,10}$")
    const val MIN_PASSWORD_LENGTH = 8

    fun email(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !EMAIL.matches(value.trim()) -> "Enter a valid email address"
        else -> null
    }

    fun password(value: String): String? = when {
        value.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        value.none { it.isDigit() } || value.none { it.isLetter() } -> "Password must contain letters and numbers"
        else -> null
    }

    fun name(value: String, field: String = "Name"): String? = when {
        value.isBlank() -> "$field is required"
        value.length > 50 -> "$field must be 50 characters or fewer"
        else -> null
    }

    fun required(value: String, field: String, max: Int = 100): String? = when {
        value.isBlank() -> "$field is required"
        value.length > max -> "$field must be $max characters or fewer"
        else -> null
    }

    fun postcode(value: String): String? = if (POSTCODE.matches(value.trim())) null else "Enter a valid postcode"

    fun heightCm(value: Double?): String? =
        if (value == null || value !in 50.0..272.0) "Enter a height between 50 and 272 cm" else null

    fun weightKg(value: Double?): String? =
        if (value == null || value !in 20.0..400.0) "Enter a weight between 20 and 400 kg" else null

    fun otp(value: String): String? = if (value.length == 6 && value.all { it.isDigit() }) null else "Enter the 6-digit code"
}
