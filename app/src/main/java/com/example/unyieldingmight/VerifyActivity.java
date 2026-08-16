package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
    }

    public void nextActivityMembership(View v){
        Intent i = new Intent(this, MembershipActivity.class);
        startActivity(i);
    }
}
