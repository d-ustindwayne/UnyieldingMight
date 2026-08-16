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
//    Object of class, named classData
    Class1 class1Data;
//    List of classData, named classList
    List<Class1> class1List;
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
                searchList(newText);
                return true;
            }

    });
//        Constructor for recycler view, where context is the activity and spanCount is the no. of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        class1List = new ArrayList<>();
        class1Data = new Class1("Boxfit", "Dara", "Heavy", R.string.boxfit, "26/05/2026", "10:00", "13:00", "0", "10",R.drawable.boxfit);
        class1List.add(class1Data);
        class1Data = new Class1("Crossfit", "Dustin", "Medium", R.string.crossfit, "27/05/2026", "12:00", "14:00", "2", "20",R.drawable.boxfit);
        class1List.add(class1Data);

        adapter = new Adapter(HomeActivity.this, class1List);
        recyclerView.setAdapter(adapter);
}
//  Search function
    private void searchList(String text){
        List<Class1> dataSearchList = new ArrayList<>();
        for (Class1 data : class1List){
            if (data.getTitle().toLowerCase().contains(text.toLowerCase())) {
                dataSearchList.add(data);
            }
        }
        if (dataSearchList.isEmpty()){
            Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setSearchList(dataSearchList);
        }
    }
}
