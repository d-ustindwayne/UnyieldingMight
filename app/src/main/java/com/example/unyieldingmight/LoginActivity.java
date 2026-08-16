package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    public void nextActivityRegister(View v){
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    public void nextActivityHome(View v){
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
    }

    public void login(View v){
//        Find email via their id
        EditText email = findViewById(R.id.activity_login_et_email);
        String emailData = email.getText().toString();

//        Find password via their id
        EditText password = findViewById(R.id.activity_login_et_password);
        String passwordData = password.getText().toString();

        Log.d(emailData,"email");
        Log.d(passwordData,"password");
    }
}
