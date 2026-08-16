package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LinkmembershipActivity extends AppCompatActivity {
    private EditText etMembershipId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkmembership);

        etMembershipId = findViewById(R.id.activity_linkmembership_et_membershipId);
    }

    public void nextActivityActivityLevel(View v) {
        String idStr = etMembershipId.getText().toString().trim();
        if (idStr.isEmpty()) {
            Toast.makeText(this, "Please enter your Membership ID", Toast.LENGTH_SHORT).show();
            return;
        }

        int membershipId;
        try {
            membershipId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid ID format", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            String firstName = getIntent().getStringExtra("FirstName");
            String lastName = getIntent().getStringExtra("LastName");

            Membership membership = Database.validateMembership(membershipId, firstName, lastName);

            runOnUiThread(() -> {
                if (membership != null) {
                    Toast.makeText(this, "Membership linked successfully!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, DoneActivity.class);
                    Bundle b = getIntent().getExtras();
                    if (b != null) i.putExtras(b);
                    i.putExtra("MembershipId", membershipId);
                    i.putExtra("Height", membership.getHeight());
                    i.putExtra("Weight", membership.getWeight());
                    i.putExtra("ActivityMultiplier", membership.getActivityMultiplier());
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Invalid ID or name mismatch. Ensure it's not already linked.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    public void previousActivityMembership(View v) {
        finish();
    }
}
