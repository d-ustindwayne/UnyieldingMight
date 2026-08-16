package com.example.unyieldingmight.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unyieldingmight.Models.Adapter;
import com.example.unyieldingmight.Models.Customer;
import com.example.unyieldingmight.Services.Database;
import com.example.unyieldingmight.Models.GymClass;
import com.example.unyieldingmight.R;
import com.example.unyieldingmight.Models.User;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    User user;
    ArrayList<GymClass> gymClasses;
    RecyclerView recyclerView;
    Adapter adapter;
    ProgressBar loadingSpinner;
    
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private boolean isLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        recyclerView = findViewById(R.id.activity_home_recyclerView);
        loadingSpinner = findViewById(R.id.home_loading_spinner);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load data dynamically
        loadGymClasses();
    }

    private void loadGymClasses() {
        loadingSpinner.setVisibility(View.VISIBLE);
        isLoaded = false;

        // 20 second timeout
        timeoutHandler.postDelayed(() -> {
            if (!isLoaded) {
                loadingSpinner.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error: Connection timeout. Could not load classes.", Toast.LENGTH_LONG).show();
            }
        }, 20000);

        new Thread(() -> {
            try {
                float tdee = 2000f;
                User current = Database.getCurrentUser();
                if (current != null) {
                    Customer customer = Database.getCustomer(current.getEmail());
                    if (customer != null) {
                        tdee = customer.getTDEE();
                        if (tdee <= 0) tdee = customer.calculateTDEE();
                    }
                }
                final float finalTdee = tdee;

                final ArrayList<GymClass> fetchedClasses = Database.getGymClassesAvailable();

                runOnUiThread(() -> {
                    isLoaded = true;
                    gymClasses = fetchedClasses;
                    loadingSpinner.setVisibility(View.GONE);

                    if (gymClasses != null && !gymClasses.isEmpty()) {
                        adapter = new Adapter(HomeActivity.this, gymClasses, finalTdee);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(HomeActivity.this, "No classes available at the moment.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(HomeActivity.this, "Error connecting to database", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
