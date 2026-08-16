package com.example.unyieldingmight.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Security {
    public static String hashData(String text) throws Exception {
        // Implement SHA-256 one-way cryptographic hashing to securely store user credentials and ensure password integrity.
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hashBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
