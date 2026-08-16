package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class StatsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Spinner spinner1 = findViewById(R.id.activity_stats_spin_height);
        Spinner spinner2 = findViewById(R.id.activity_stats_spin_weight);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                this,
                R.array.height_array,
                R.layout.my_selected_item
        );
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this,
                R.array.weight_array,
                R.layout.my_selected_item
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
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
