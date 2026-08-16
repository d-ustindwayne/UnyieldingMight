package com.example.unyieldingmight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class StatsActivity extends AppCompatActivity {
    private EditText etHeight, etWeight;
    private Spinner spinHeightUnit, spinWeightUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        etHeight = findViewById(R.id.activity_stats_et_height);
        etWeight = findViewById(R.id.activity_stats_et_weight);
        spinHeightUnit = findViewById(R.id.activity_stats_spin_height);
        spinWeightUnit = findViewById(R.id.activity_stats_spin_weight);

        ArrayAdapter<CharSequence> adapterHeight = ArrayAdapter.createFromResource(this,
                R.array.height_array, android.R.layout.simple_spinner_item);
        adapterHeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinHeightUnit.setAdapter(adapterHeight);

        ArrayAdapter<CharSequence> adapterWeight = ArrayAdapter.createFromResource(this,
                R.array.weight_array, android.R.layout.simple_spinner_item);
        adapterWeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinWeightUnit.setAdapter(adapterWeight);
    }

    public void previousActivityProfile(View v) {
        finish();
    }

    public void proceedToActivityLevel(View v) {
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter your height and weight", Toast.LENGTH_SHORT).show();
            return;
        }

        float height = Float.parseFloat(heightStr);
        float weight = Float.parseFloat(weightStr);

        String hUnit = spinHeightUnit.getSelectedItem().toString();
        String wUnit = spinWeightUnit.getSelectedItem().toString();

        if (hUnit.equals("m")) {
            height *= 100f;
        } else if (hUnit.equals("in")) {
            height *= 2.54f;
        } else if (hUnit.equals("feet")) {
            height *= 30.48f;
        }

        if (wUnit.equals("lb")) {
            weight *= 0.453592f;
        }

        Intent i = new Intent(this, ActivitylevelActivity.class);
        Bundle b = getIntent().getExtras();
        if (b != null) i.putExtras(b);
        i.putExtra("Height", height);
        i.putExtra("Weight", weight);
        startActivity(i);
    }
}
