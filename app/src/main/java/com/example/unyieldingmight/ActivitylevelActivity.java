package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class ActivitylevelActivity extends AppCompatActivity {
    private SeekBar sbActivityLevel;
    private TextView tvDescription;
    private float multiplier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activitylevel);

        sbActivityLevel = findViewById(R.id.activity_activitylevel_sb_activityLevel);
        tvDescription = findViewById(R.id.activity_activitylevel_tv_description);

        sbActivityLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        multiplier = 1.2f;
                        tvDescription.setText("Little to no exercise.");
                        break;
                    case 1:
                        multiplier = 1.375f;
                        tvDescription.setText("Light exercise/sports 1-3 days/week.");
                        break;
                    case 2:
                        multiplier = 1.55f;
                        tvDescription.setText("Moderate exercise/sports 3-5 days/week.");
                        break;
                    case 3:
                        multiplier = 1.725f;
                        tvDescription.setText("Hard exercise/sports 6-7 days/week.");
                        break;
                    case 4:
                        multiplier = 1.9f;
                        tvDescription.setText("Very hard exercise, physical job or training 2x/day.");
                        break;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void nextActivityDone(View v) {
        Intent i = new Intent(this, DoneActivity.class);
        i.putExtras(Objects.requireNonNull(getIntent().getExtras()));
        i.putExtra("ActivityMultiplier", multiplier);
        startActivity(i);
    }

    public void previousActivityStats(View v) {
        finish();
    }
}
