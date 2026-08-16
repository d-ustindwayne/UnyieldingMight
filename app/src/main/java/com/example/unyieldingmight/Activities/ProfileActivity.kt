package com.example.unyieldingmight.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.unyieldingmight.R;

import java.util.stream.Stream;

public class ProfileActivity extends AppCompatActivity {
    private String email, password, firstName, lastName;
    private EditText etDay, etMonth, etYear, etStreet, etCity, etRegion, etPostcode;
    private Spinner spCountry;
    @Override
    protected void onCreate(Bundle savedInstanceRegion) {
        super.onCreate(savedInstanceRegion);
        setContentView(R.layout.activity_profile);

        firstName = getIntent().getStringExtra("FirstName");
        lastName = getIntent().getStringExtra("LastName");
        email = getIntent().getStringExtra("Email");
        password = getIntent().getStringExtra("Password");

        etDay = findViewById(R.id.activity_profile_et_day);
        etMonth = findViewById(R.id.activity_profile_et_month);
        etYear = findViewById(R.id.activity_profile_et_year);
        etStreet = findViewById(R.id.activity_profile_et_street);
        etCity = findViewById(R.id.activity_profile_et_city);
        etRegion = findViewById(R.id.activity_profile_et_region);
        etPostcode = findViewById(R.id.activity_profile_et_postcode);
        spCountry = findViewById(R.id.activity_profile_spin_country);

        ArrayAdapter<CharSequence> adapterCountry = ArrayAdapter.createFromResource(this,
                R.array.country_array, android.R.layout.simple_spinner_item);
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(adapterCountry);
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
        String region = etRegion.getText().toString().trim();
        String postcode = etPostcode.getText().toString().trim();
        
        if (spCountry.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
            return;
        }
        String country = spCountry.getSelectedItem().toString();

        boolean isEmpty = Stream.of(day, month, year, street, city, region, postcode).anyMatch(String::isEmpty);
        if(isEmpty){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format DOB for SQL: YYYY-MM-DD
        String dob = String.format("%s-%s-%s", year, month, day);

        Intent i = new Intent(this, StatsActivity.class);
        i.putExtras(getIntent().getExtras());
        i.putExtra("DOB", dob);
        i.putExtra("Street", street);
        i.putExtra("City", city);
        i.putExtra("Region", region);
        i.putExtra("Postcode", postcode);
        i.putExtra("Country", country);
        startActivity(i);
    }
}
