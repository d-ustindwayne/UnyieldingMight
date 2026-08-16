package com.example.unyieldingmight;

import brevo.ApiClient;
import brevo.Configuration;
import brevo.auth.ApiKeyAuth;

public class SendEmailOTP {
    public void sendVerification(String email) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(BuildConfig.BREVO_APIKEY);
    }
}