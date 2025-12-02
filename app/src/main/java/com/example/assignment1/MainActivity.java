package com.example.assignment1; // Replace with your actual package name

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TrainAdapter adapter;
    private List<Train> trainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Setup Toolbar so it acts as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Hide default title because we used a custom TextView centered in XML
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 2. Setup TabLayout icons (optional aesthetic fix)
        // The XML sets the icons, but sometimes they need nudging to look like the image
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        // Ensure the first tab is selected by default (Departure)
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tab.select();
        }

        // 3. Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewTrains);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 4. Create Dummy Data matching the image
        trainList = new ArrayList<>();
        trainList.add(new Train("JANMABHOOMI EXP", "12806", "SC", "GNT", "07:10", "11:50", "04:40"));
        trainList.add(new Train("SC GNT EXP", "12706", "SC", "GNT", "07:40", "14:25", "06:45"));
        trainList.add(new Train("SABARI EXP", "17230", "HYB", "GNT", "11:15", "17:00", "05:45"));
        // Added one more to show scrolling
        trainList.add(new Train("GOLCONDA EXP", "17202", "SC", "GNT", "12:30", "18:10", "05:40"));


        // 5. Set Adapter
        adapter = new TrainAdapter(trainList);
        recyclerView.setAdapter(adapter);
    }
}