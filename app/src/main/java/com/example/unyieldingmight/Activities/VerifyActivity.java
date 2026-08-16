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
                        // Check if the email is "safe to send" and not disposable
                        if ("true".equalsIgnoreCase(data.safe_to_send()) && !"true".equalsIgnoreCase(data.disposable())) {
                            Log.d(TAG, "Email is safe. Sending OTP.");
                            sendOTP();
                        } else {
                            String reason = data.reason() != null ? data.reason() : "Invalid or risky email";
                            Log.w(TAG, "Email risky. Reason: " + reason);
                            Toast.makeText(VerifyActivity.this, "Security Check Failed: " + reason, Toast.LENGTH_LONG).show();
                            // If it's a test account, or you want to bypass this during dev,
                            // you could call sendOTP() here anyway or just finish.
                            finish();
                        }
                    } else {
                        // If API check fails (e.g. invalid API key, network error), 
                        // fallback to sending OTP anyway so we don't block the user.
                        Log.e(TAG, "Email verification API failed or returned error. Falling back to send OTP.");
                        if (rawResponse != null && rawResponse.contains("invalid_api_key")) {
                            Toast.makeText(this, "Verification API error: Invalid API Key. Falling back...", Toast.LENGTH_SHORT).show();
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
        String otp1Data = otp1.getText().toString().trim();

        EditText otp2 = findViewById(R.id.activity_verify_et_number2);
        String otp2Data = otp2.getText().toString().trim();

        EditText otp3 = findViewById(R.id.activity_verify_et_number3);
        String otp3Data = otp3.getText().toString().trim();

        EditText otp4 = findViewById(R.id.activity_verify_et_number4);
        String otp4Data = otp4.getText().toString().trim();

        EditText otp5 = findViewById(R.id.activity_verify_et_number5);
        String otp5Data = otp5.getText().toString().trim();

        EditText otp6 = findViewById(R.id.activity_verify_et_number6);
        String otp6Data = otp6.getText().toString().trim();

        // On input for each box
        otp1.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) otp2.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        otp2.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) otp3.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        otp3.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) otp4.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        otp4.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) otp5.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        otp5.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) otp5.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Backspace for each box
        otp6.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && otp6.getText().length() == 0) {
                otp5.requestFocus();
            }
            return false;
        });
        otp5.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && otp5.getText().length() == 0) {
                otp4.requestFocus();
            }
            return false;
        });
        otp4.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && otp4.getText().length() == 0) {
                otp3.requestFocus();
            }
            return false;
        });
        otp3.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && otp3.getText().length() == 0) {
                otp2.requestFocus();
            }
            return false;
        });
        otp2.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && otp2.getText().length() == 0) {
                otp1.requestFocus();
            }
            return false;
        });

// Repeat this for et2 (to et3), et3 (to et4), etc.

        if(otp1Data.isEmpty() || otp2Data.isEmpty() || otp3Data.isEmpty() || otp4Data.isEmpty() || otp5Data.isEmpty() || otp6Data.isEmpty()){
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            String inputOtp = otp1Data + otp2Data + otp3Data + otp4Data + otp5Data + otp6Data;
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
