package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ActivitylevelActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activitylevel);
    }
    public void nextActivityDone(View v){
        Intent i = new Intent(this, DoneActivity.class);
        startActivity(i);
    }
    public void previousActivityStats(View v){
        Intent i = new Intent(this, StatsActivity.class);
        startActivity(i);
    }
}
