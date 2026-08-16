package com.example.unyieldingmight.Services;

import android.util.Log;

import com.example.unyieldingmight.BuildConfig;
import com.example.unyieldingmight.Models.EmailVerificationData;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailVerification {

    private String apiKey = BuildConfig.QEV_APIKEY;
    private String apiURI = "https://api.quickemailverification.com/v1/verify?";
    private String email;
    private StringBuilder responseString;
    private int responseCode;

    // Set the email to the object
    public EmailVerification email(String email) {
        this.email = email;
        return this;
    }

    public EmailVerification verify() {
        if (email == null) { return null; }

        // Creating the whole URL
        responseString = new StringBuilder();
        String urlString = String.format("%semail=%s&apikey=%s", apiURI, email, apiKey);
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlString);
            // Creates a connection to the URL and gets its response
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get the response and be stored as a string
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    responseString.append(line);
                }
                // Prevent memory leak
                reader.close();
            }
        } catch (Exception e) {
            Log.e(e.getMessage(), "VERIFICATION ERROR");
        } finally {
            // Close the connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return this;
    }

    public EmailVerificationData getData() {
        // Map the response from JSON to EmailVerificationData class
        try {
            Gson gson = new Gson();
            return gson.fromJson(responseString.toString(), EmailVerificationData.class);
        } catch (Exception e) {
            this.responseString.append(e.getMessage());
            return null;
        }
    }

    public String getResponseString() { return responseString.toString(); }

    public int getResponseCode() { return responseCode; }
}