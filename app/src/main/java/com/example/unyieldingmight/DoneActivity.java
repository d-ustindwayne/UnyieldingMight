package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Date;

public class DoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);
    }

    public void nextActivityHome(View v) {
        Bundle b = getIntent().getExtras();
        if (b == null) return;

        String fName = b.getString("FirstName");
        String lName = b.getString("LastName");
        String email = b.getString("Email");
        String password = b.getString("Password");
        String dob = b.getString("DOB");
        String street = b.getString("Street");
        String suburb = b.getString("Suburb");
        String city = b.getString("City");
        String country = b.getString("Country");
        String postcode = b.getString("Postcode");
        String gender = b.getString("Gender");
        float height = b.getFloat("Height");
        float weight = b.getFloat("Weight");
        float multiplier = b.getFloat("ActivityMultiplier");
        Integer membershipId = b.containsKey("MembershipId") ? b.getInt("MembershipId") : null;

        new Thread(() -> {
            boolean success = Database.registerCustomer(
                fName, lName, email, password,
                street, suburb, city, country, postcode,
                height, weight, multiplier, membershipId,
                Date.valueOf(dob), gender
            );

            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}
