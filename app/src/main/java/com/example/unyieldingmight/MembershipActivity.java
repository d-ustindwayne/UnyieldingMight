package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MembershipActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);
    }
    public void nextActivityProfile(View v){
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }
    public void nextActivityLinkMembership(View v){
        Intent i = new Intent(this, LinkmembershipActivity.class);
        startActivity(i);
    }
}
