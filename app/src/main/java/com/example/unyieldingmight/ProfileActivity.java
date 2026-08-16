package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Spinner spinner = findViewById(R.id.activity_profile_spin_country);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.country_array,
                R.layout.my_selected_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    public void nextActivityStats(View v){
        Intent i = new Intent(this, StatsActivity.class);
        startActivity(i);
    }
    public void previousActivityMembership(View v){
        Intent i = new Intent(this, MembershipActivity.class);
        startActivity(i);
    }
}