package com.example.unyieldingmight.Services;

import com.example.unyieldingmight.BuildConfig;

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

    public void send(String toEmail) {
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
