package com.example.unyieldingmight;

import java.util.Random;

public class EmailOTP extends EmailFunction {
    private final String OTP;
    private boolean authenticated = false;

    public EmailOTP() {
        super();
        this.OTP = generateOTP();
    }

    public boolean isAuthenticated() { return authenticated; }

    public boolean validateOTP(String inputOTP) {
        this.authenticated = OTP.equals(inputOTP);
        return authenticated;
    }

    public String generateOTP() {
        Random rand = new Random();
        int n = rand.nextInt(1000000);
        return String.format("%06d", n);
    }
}