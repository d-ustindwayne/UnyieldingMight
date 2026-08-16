package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivitylevelActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activitylevel);

        SeekBar seekBar = findViewById(R.id.activity_activitylevel_sb_activityLevel);
        TextView textView = findViewById(R.id.activity_activitylevel_tv_description);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the TextView content with the current progress
                if (progress==0) {
                    textView.setText("Little to no exercise.");
                }
                else if (progress==1) {
                    textView.setText("Exercise 1-3 days per week.");
                }
                else if (progress==2) {
                    textView.setText("Exercise 3-5 days per week.");
                }
                else if (progress==3) {
                    textView.setText("Exercise 6-7 days per week.");
                }
                else if (progress==4) {
                    textView.setText("Vigorous training two times a day.");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: Action when user starts dragging
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Action when user stops dragging
            }
        });
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
