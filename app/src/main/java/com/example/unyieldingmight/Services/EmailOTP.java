package com.example.unyieldingmight.Services;

import android.annotation.SuppressLint;

import com.example.unyieldingmight.BuildConfig;

import java.util.Random;

public class EmailOTP extends EmailFunction {
    private final String OTP;
    private boolean authenticated = false;

    public EmailOTP() {
        super();
        // Creates a random number once called
        this.OTP = generateOTP();
    }

    public boolean isAuthenticated() { return authenticated; } // Check authenticity

    public boolean validateOTP(String inputOTP) {
        this.authenticated = OTP.equals(inputOTP);
        return authenticated;
    }

    @SuppressLint("DefaultLocale")
    public String generateOTP() {
        // Generate a random number between 0-999999
        Random rand = new Random();
        int n = rand.nextInt(1000000);
        return String.format("%06d", n);
    }

    public void send(String toEmail) {
        // Use functions from the email function class
        // Send OTP to the user's email
        setSender("Verify from Unyielding Might", BuildConfig.SMTP_USER);
        String name = toEmail.split("@")[0];
        setReceiver(name, toEmail);
        createEmail("Your Verification Code", "Your OTP code is: <b>" + OTP + "</b>");
        sendEmail();
    }

    public String getOTP() {
        return OTP;
    }
}
