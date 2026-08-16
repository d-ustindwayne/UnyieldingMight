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
    public void proceed(View v){
        EditText day = findViewById(R.id.activity_profile_et_day);
        String dayData = day.getText().toString().trim();

        EditText month = findViewById(R.id.activity_profile_et_month);
        String monthData = month.getText().toString().trim();

        EditText year = findViewById(R.id.activity_profile_et_year);
        String yearData = year.getText().toString().trim();

        EditText street = findViewById(R.id.activity_profile_et_street);
        String streetData = street.getText().toString().trim();

        EditText city = findViewById(R.id.activity_profile_et_city);
        String cityData = city.getText().toString().trim();

        EditText state = findViewById(R.id.activity_profile_et_state);
        String stateData = state.getText().toString().trim();

        EditText postCode = findViewById(R.id.activity_profile_et_postcode);
        String postCodeData = postCode.getText().toString().trim();

//        Spinner for country
        Spinner country = findViewById(R.id.activity_profile_spin_country);
        String countryData = country.getSelectedItem().toString();

        Log.d(countryData, "Spinner Value");

        Intent i = new Intent(this, StatsActivity.class);
        startActivity(i);}
    public void previousActivityMembership(View v){
        Intent i = new Intent(this, MembershipActivity.class);
        startActivity(i);
    }
}