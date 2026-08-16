package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyActivity extends AppCompatActivity {
    private String email;
    private String password;
    private EmailOTP emailOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        email = getIntent().getStringExtra("Email");
        password = getIntent().getStringExtra("Password");

        if (email == null) {
            Toast.makeText(this, "Error: Email missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        emailOTP = new EmailOTP();
        
        // Verify email existence and security before sending OTP
        new Thread(() -> {
            EmailVerification verification = new EmailVerification().email(email).verify();
            EmailVerificationData data = verification.getData();

            runOnUiThread(() -> {
                if (data != null) {
                    // Check if the email is "safe to send" and not disposable
                    if ("true".equalsIgnoreCase(data.safe_to_send()) && !"true".equalsIgnoreCase(data.disposable())) {
                        sendOTP();
                    } else {
                        String reason = data.reason() != null ? data.reason() : "Invalid or risky email";
                        Toast.makeText(VerifyActivity.this, "Email verification failed: " + reason, Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    // If API check fails, fallback to sending OTP anyway or show error
                    Log.e("VerifyActivity", "Email verification API returned null");
                    sendOTP(); 
                }
            });
        }).start();
    }

    private void sendOTP() {
        new Thread(() -> {
            emailOTP.send(email);
            runOnUiThread(() -> {
                Toast.makeText(VerifyActivity.this, "Verification code sent to " + email + ". Check your email inbox (or spam folder) to continue.", Toast.LENGTH_LONG).show();
            });
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

        if(otp1Data.isEmpty() || otp2Data.isEmpty() || otp3Data.isEmpty() || otp4Data.isEmpty() || otp5Data.isEmpty() || otp6Data.isEmpty()){
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            String inputOtp = otp1Data + otp2Data + otp3Data + otp4Data + otp5Data + otp6Data;
            
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
