package com.example.unyieldingmight.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unyieldingmight.Services.EmailOTP;
import com.example.unyieldingmight.Services.EmailVerification;
import com.example.unyieldingmight.Models.EmailVerificationData;
import com.example.unyieldingmight.R;

public class VerifyActivity extends AppCompatActivity {
    private String email;
    private String password;
    private EmailOTP emailOTP;
    private static final String TAG = "VerifyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        email = getIntent().getStringExtra("Email");
        password = getIntent().getStringExtra("Password");

        Log.d(TAG, "Email: " + email);

        if (email == null) {
            Toast.makeText(this, "Error: Email missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initOtpFields();
        emailOTP = new EmailOTP();
        
        // Show a "Loading" or "Verifying" message
        Toast.makeText(this, "Verifying email security...", Toast.LENGTH_SHORT).show();

        // Verify email existence and security before sending OTP
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting EmailVerification for: " + email);
                EmailVerification verification = new EmailVerification().email(email).verify();
                EmailVerificationData data = verification.getData();
                
                String rawResponse = verification.getResponseString();
                Log.d(TAG, "QEV Response: " + rawResponse);

                runOnUiThread(() -> {
                    if (data != null && data.success() != null && "true".equalsIgnoreCase(data.success())) {
                        boolean isSafe = "true".equalsIgnoreCase(data.safe_to_send());
                        boolean isValid = "valid".equalsIgnoreCase(data.result());
                        boolean isDisposable = "true".equalsIgnoreCase(data.disposable());

                        if ((isSafe || isValid) && !isDisposable) {
                            Log.d(TAG, "Email is valid/safe. Sending OTP.");
                            sendOTP();
                        } else {
                            String reason = data.reason() != null ? data.reason() : "Invalid or risky email";
                            Log.w(TAG, "Email risky. Reason: " + reason);
                            Toast.makeText(VerifyActivity.this, "Security Check Failed: " + reason, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        // If API check fails (e.g. invalid API key, network error), 
                        // fallback to sending OTP anyway so we don't block the user.
                        Log.e(TAG, "Email verification API failed or returned error. Falling back to send OTP.");
                        if (rawResponse != null && rawResponse.contains("invalid_api_key")) {
                            Toast.makeText(this, "Verification API error: Invalid API Key.", Toast.LENGTH_SHORT).show();
                        }
                        sendOTP();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error in verification thread", e);
                runOnUiThread(this::sendOTP);
            }
        }).start();
    }

    private void initOtpFields() {
        EditText otp1 = findViewById(R.id.activity_verify_et_number1);
        EditText otp2 = findViewById(R.id.activity_verify_et_number2);
        EditText otp3 = findViewById(R.id.activity_verify_et_number3);
        EditText otp4 = findViewById(R.id.activity_verify_et_number4);
        EditText otp5 = findViewById(R.id.activity_verify_et_number5);
        EditText otp6 = findViewById(R.id.activity_verify_et_number6);

        setupAutoMove(otp1, otp2);
        setupAutoMove(otp2, otp3);
        setupAutoMove(otp3, otp4);
        setupAutoMove(otp4, otp5);
        setupAutoMove(otp5, otp6);

        setupBackMove(otp2, otp1);
        setupBackMove(otp3, otp2);
        setupBackMove(otp4, otp3);
        setupBackMove(otp5, otp4);
        setupBackMove(otp6, otp5);
    }

    private void setupAutoMove(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) next.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBackMove(EditText current, EditText previous) {
        current.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (current.getText().toString().isEmpty()) {
                    previous.requestFocus();
                }
            }
            return false;
        });
    }

    private void sendOTP() {
        Toast.makeText(this, "Sending verification code...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                emailOTP.send(email);
                String response = emailOTP.getResponseString();
                Log.d(TAG, "SMTP Response: " + response);

                runOnUiThread(() -> {
                    if (response != null && response.startsWith("Success")) {
                        Toast.makeText(VerifyActivity.this, "Code sent to " + email + ". Check your inbox (or spam).", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(VerifyActivity.this, "Mail Error: " + response, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failed to send email: " + response);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error in send thread", e);
                runOnUiThread(() -> Toast.makeText(this, "Internal error sending mail.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void verify(View v){
        EditText otp1 = findViewById(R.id.activity_verify_et_number1);
        EditText otp2 = findViewById(R.id.activity_verify_et_number2);
        EditText otp3 = findViewById(R.id.activity_verify_et_number3);
        EditText otp4 = findViewById(R.id.activity_verify_et_number4);
        EditText otp5 = findViewById(R.id.activity_verify_et_number5);
        EditText otp6 = findViewById(R.id.activity_verify_et_number6);

        String inputOtp = otp1.getText().toString().trim() +
                          otp2.getText().toString().trim() +
                          otp3.getText().toString().trim() +
                          otp4.getText().toString().trim() +
                          otp5.getText().toString().trim() +
                          otp6.getText().toString().trim();

        if(inputOtp.length() < 6){
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            Log.d(TAG, "User input OTP: " + inputOtp + " | Expected: " + emailOTP.getOTP());
            
            if (emailOTP.validateOTP(inputOtp)) {
                Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MembershipActivity.class);
                i.putExtras(getIntent().getExtras());
                startActivity(i);
            } else {
                Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
