package com.example.unyieldingmight.Models

data class EmailVerificationData(
    val result: String? = null,
    val reason: String? = null,
    val disposable: String? = null,
    val accept_all: String? = null,
    val role: String? = null,
    val free: String? = null,
    val email: String? = null,
    val user: String? = null,
    val domain: String? = null,
    val mx_record: String? = null,
    val mx_domain: String? = null,
    val safe_to_send: String? = null,
    val did_you_mean: String? = null,
    val success: String? = null,
    val message: String? = null
)
