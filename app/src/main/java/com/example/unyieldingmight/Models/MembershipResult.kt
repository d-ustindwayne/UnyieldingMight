package com.example.unyieldingmight.Models;

public class MembershipResult {
    public enum Status {
        SUCCESS,
        NOT_FOUND,
        EMAIL_MISMATCH,
        ALREADY_LINKED
    }

    private final Status status;
    private final Membership membership;

    public MembershipResult(Status status, Membership membership) {
        this.status = status;
        this.membership = membership;
    }

    public Status getStatus() {
        return status;
    }

    public Membership getMembership() {
        return membership;
    }
}
