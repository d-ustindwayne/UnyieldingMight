package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class StatsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
    }
    public void nextActivityActivityLevel(View v){
        Intent i = new Intent(this, ActivitylevelActivity.class);
        startActivity(i);
    }
    public void previousActivityProfile(View v){
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }
}
