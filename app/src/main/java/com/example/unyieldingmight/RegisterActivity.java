package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void nextActivityLogin(View v){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void nextActivityVerify(View v){
        Intent i = new Intent(this, VerifyActivity.class);
        startActivity(i);
    }

    public void register(View v){
        EditText email = findViewById(R.id.activity_register_et_email);
        String emailData = email.getText().toString();

        EditText password = findViewById(R.id.activity_register_et_password);
        String passwordData = password.getText().toString();

        EditText confirmPassword = findViewById(R.id.activity_register_et_confirmPassword);
        String confirmPasswordData = confirmPassword.getText().toString();

        Intent i = new Intent(this, VerifyActivity.class);
        i.putExtra(emailData,"Email");
        i.putExtra(passwordData, "Password");

        if(passwordData.equals(confirmPasswordData)){
            startActivity(i);
        }
    }
}
