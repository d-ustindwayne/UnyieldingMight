package com.example.unyieldingmight;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    User user;
//    Object of class, named classData
//    List of classData, named classList
    ArrayList<GymClass> class1List;
    RecyclerView recyclerView;
    Adapter adapter;
//    UI widget
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        recyclerView = findViewById(R.id.activity_home_recyclerView);
        searchView = findViewById(R.id.searchBar);
//        Hides the phone's keyboard upon loading the activity
        searchView.clearFocus();
//        Detects user input whenever they interact with the search bar
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
//            Calls the search function, searchList(); whenever, the user inputs smthg into the search bar
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

    });
//        Constructor for recycler view, where context is the activity and spanCount is the no. of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        class1List = Database.getGymClassesAvailable();

        adapter = new Adapter<GymClass>(HomeActivity.this, class1List);
        recyclerView.setAdapter(adapter);
    }
}
