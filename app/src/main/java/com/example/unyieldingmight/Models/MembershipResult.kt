package com.example.unyieldingmight.Models

class MembershipResult(@JvmField val status: Status?, @JvmField val membership: Membership?) {
    enum class Status {
        SUCCESS,
        NOT_FOUND,
        EMAIL_MISMATCH,
        ALREADY_LINKED
    }
}
