package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.activity_login_et_email);
        passwordEditText = findViewById(R.id.activity_login_et_password);
        loginButton = findViewById(R.id.activity_login_btn_login);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Prevent going back to splash/main
            }
        });
    }

    public void nextActivityRegister(View v){
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    public void login(View v) {
        String emailData = emailEditText.getText().toString().trim();
        String passwordData = passwordEditText.getText().toString().trim();

        if (emailData.isEmpty() || passwordData.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        new Thread(() -> {
            try {
                User user = Database.loginUser(emailData, passwordData);
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText(R.string.btn_login);

                    if (user != null) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(this, HomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    } else {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.e("LoginActivity", "Login error", e);
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText(R.string.btn_login);
                    Toast.makeText(this, "A connection error occurred", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
