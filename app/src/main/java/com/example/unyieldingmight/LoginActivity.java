package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
}
