package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

    public void previousActivityProfile(View v){
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

    public void proceedToActivityLevel(View v){
//        height data
        EditText height =  findViewById(R.id.activity_stats_et_height);
        String heightString = height.getText().toString().trim();
        float heightData = Float.parseFloat(heightString);
//        (cm, m, inch)
        Spinner hMeasure = findViewById(R.id.activity_stats_spin_height);
        String hMeasureData = hMeasure.getSelectedItem().toString();
//        weight data
        EditText weight = findViewById(R.id.activity_stats_et_weight);
        String weightString = weight.getText().toString().trim();
        float weightData = Float.parseFloat(weightString);
//        (kg)
        Spinner wMeasure = findViewById(R.id.activity_stats_spin_weight);
        String wMeasureData = wMeasure.getSelectedItem().toString();

        Log.d(hMeasureData, "Height Unit");
        Log.d(wMeasureData, "Measure Unit");

        Log.d(String.valueOf(heightData), "Height");
        Log.d(String.valueOf(weightData), "Measure");

        Intent i = new Intent(this, ActivitylevelActivity.class);
        startActivity(i);
    }
}
