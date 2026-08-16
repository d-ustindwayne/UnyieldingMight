package com.example.unyieldingmight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {
    private String email, password;
    private EditText etDay, etMonth, etYear, etStreet, etCity, etState, etPostcode;
    private Spinner spCountry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        email = getIntent().getStringExtra("Email");
        password = getIntent().getStringExtra("Password");

        etDay = findViewById(R.id.activity_profile_et_day);
        etMonth = findViewById(R.id.activity_profile_et_month);
        etYear = findViewById(R.id.activity_profile_et_year);
        etStreet = findViewById(R.id.activity_profile_et_street);
        etCity = findViewById(R.id.activity_profile_et_city);
        etState = findViewById(R.id.activity_profile_et_state);
        etPostcode = findViewById(R.id.activity_profile_et_postcode);
        Spinner spCountry = findViewById(R.id.activity_profile_spin_country);
    }

    public void previousActivityMembership(View v) {
        finish();
    }

    public void proceed(View v) {
        String day = etDay.getText().toString().trim();
        String month = etMonth.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String postcode = etPostcode.getText().toString().trim();
        String country = spCountry.getSelectedItem().toString();

        if (day.isEmpty() || month.isEmpty() || year.isEmpty() || street.isEmpty() || 
            city.isEmpty() || state.isEmpty() || postcode.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format DOB for SQL: YYYY-MM-DD
        String dob = String.format("%s-%s-%s", year, month, day);

        Intent i = new Intent(this, StatsActivity.class);
        i.putExtra("Email", email);
        i.putExtra("Password", password);
        i.putExtra("DOB", dob);
        i.putExtra("Street", street);
        i.putExtra("City", city);
        i.putExtra("Suburb", state);
        i.putExtra("Postcode", postcode);
        i.putExtra("Country", country);
        startActivity(i);
    }
}
