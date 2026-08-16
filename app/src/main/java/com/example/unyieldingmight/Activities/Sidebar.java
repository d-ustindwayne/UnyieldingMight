package com.example.unyieldingmight.Activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.unyieldingmight.R;
import com.google.android.material.navigation.NavigationView;


public class Sidebar extends AppCompatActivity {
    // Declare the DrawerLayout, NavigationView, and Toolbar
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the activity_main layout
        setContentView(R.layout.activity_main);

        // Initialize the DrawerLayout, Toolbar, and NavigationView
//        drawerLayout = findViewById(R.id.activity_sidebar_drawer_layout);
//        toolbar = findViewById(R.id.activity_sidebar_toolbar);
//        navigationView = findViewById(R.id.activity_sidebar_nav_view);

        // Create an ActionBarDrawerToggle to handle
        // the drawer's open/close state
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);

        // Add the toggle as a listener to the DrawerLayout
        drawerLayout.addDrawerListener(toggle);

        // Synchronize the toggle's state with the linked DrawerLayout
        toggle.syncState();

        // Set a listener for when an item in the NavigationView is selected
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // Called when an item in the NavigationView is selected.
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle the selected item based on its ID
                if (item.getItemId() == R.id.menu_sidebar_home) {
                    // Show a Toast message for the Account item
                    Toast.makeText(Sidebar.this,
                            "Home", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.menu_sidebar_account) {
                    // Show a Toast message for the Settings item
                    Toast.makeText(Sidebar.this,
                            "Account", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.menu_sidebar_termsandcondition) {
                    // Show a Toast message for the Settings item
                    Toast.makeText(Sidebar.this,
                            "Terms and Condition", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.menu_sidebar_logout) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(Sidebar.this,
                            "You are Logged Out", Toast.LENGTH_SHORT).show();
                }

                // Close the drawer after selection
                drawerLayout.closeDrawers();
                // Indicate that the item selection has been handled
                return true;
            }
        });

        // Add a callback to handle the back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            // Called when the back button is pressed.
            @Override
            public void handleOnBackPressed() {
                // Check if the drawer is open
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // Close the drawer if it's open
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Finish the activity if the drawer is closed
                    finish();
                }
            }
        });
    }
}
