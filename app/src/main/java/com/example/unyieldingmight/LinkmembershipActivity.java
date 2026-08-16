package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

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
            String email = getIntent().getStringExtra("Email");

            MembershipResult result = Database.validateMembership(membershipId, email);

            runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(this, "Invalid membership ID.", Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (result.getStatus()) {
                    case SUCCESS:
                        Membership membership = result.getMembership();
                        Toast.makeText(this, "Membership linked successfully!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(this, DoneActivity.class);
                        i.putExtras(Objects.requireNonNull(getIntent().getExtras()));
                        i.putExtra("MembershipId", membershipId);
                        i.putExtra("Height", membership.getHeight());
                        i.putExtra("Weight", membership.getWeight());
                        i.putExtra("ActivityMultiplier", membership.getActivityMultiplier());
                        i.putExtra("TDEE", membership.getTdee());
                        startActivity(i);
                        break;
                    case NOT_FOUND:
                        Toast.makeText(this, "Membership ID not found.", Toast.LENGTH_LONG).show();
                        break;
                    case EMAIL_MISMATCH:
                        Toast.makeText(this, "Email on record does not match your input.", Toast.LENGTH_LONG).show();
                        break;
                    case ALREADY_LINKED:
                        Toast.makeText(this, "This membership is already linked to another account.", Toast.LENGTH_LONG).show();
                        break;
                }
            });
        }).start();
    }

    public void previousActivityMembership(View v) {
        finish();
    }
}
