package com.example.unyieldingmight.models

data class User(
    val ID: Int = 0,
    val username: String? = null,
    val password: String? = null,
    val email: String? = null,
    val userClass: Role? = null
) {
    enum class Role {
        ADMIN, CUSTOMER
    }

    val isAdmin: Boolean
        get() = userClass == Role.ADMIN

    val isCustomer: Boolean
        get() = userClass == Role.CUSTOMER
}

