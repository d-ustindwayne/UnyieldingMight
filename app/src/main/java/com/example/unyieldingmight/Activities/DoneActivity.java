package com.example.unyieldingmight.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unyieldingmight.Services.Database;
import com.example.unyieldingmight.R;

import java.sql.Date;

public class DoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);
    }

    public void nextActivityHome(View v) {
        // Disable double-submits
        v.setEnabled(false);
        
        Bundle b = getIntent().getExtras();
        if (b == null) {
            v.setEnabled(true);
            return;
        }

        String fName = b.getString("FirstName");
        String lName = b.getString("LastName");
        String email = b.getString("Email");
        String password = b.getString("Password");
        String dob = b.getString("DOB");
        String street = b.getString("Street");
        String region = b.getString("Region");
        String city = b.getString("City");
        String country = b.getString("Country");
        String postcode = b.getString("Postcode");
        String gender = b.getString("Gender");
        Integer membershipId = b.containsKey("MembershipId") ? b.getInt("MembershipId") : null;
        float height = b.getFloat("Height");
        float weight = b.getFloat("Weight");
        float multiplier = b.getFloat("ActivityMultiplier");

        new Thread(() -> {
            boolean success = Database.registerCustomer(
                fName, lName, email, password,
                street, region, city, country, postcode,
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
