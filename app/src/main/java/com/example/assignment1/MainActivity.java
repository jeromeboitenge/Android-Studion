package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ComputerAdapter.OnComputerClickListener {

    private RecyclerView recyclerView;
    private ComputerAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DB and Views
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.main_recycler_view);
        emptyView = findViewById(R.id.empty_view_main);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup Buttons (Simulation of Tabs)
        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        Button btnComputers = findViewById(R.id.btn_nav_computers);
        Button btnAdd = findViewById(R.id.btn_nav_add);
        Button btnBrands = findViewById(R.id.btn_nav_brands);
        Button btnNetwork = findViewById(R.id.btn_nav_network);

        // 1. Computers (Refresh List)
        btnComputers.setOnClickListener(v -> {
            loadComputers();
            Toast.makeText(this, "Refreshed List", Toast.LENGTH_SHORT).show();
        });

        // 2. Add New -> Activity3
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Activity3.class);
            intent.putExtra("mode", "ADD");
            startActivity(intent);
        });

        // 3. Brands -> ActivityBrands
        btnBrands.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActivityBrands.class);
            startActivity(intent);
        });

        // 4. Network -> NetworkActivity
        btnNetwork.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NetworkActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComputers();
    }

    private void loadComputers() {
        List<Computer> computers = dbHelper.getAllComputers();
        if (computers.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new ComputerAdapter(this, computers, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(computers);
            }
        }
    }

    @Override
    public void onComputerClick(long id) {
        Intent intent = new Intent(this, Activity3.class);
        intent.putExtra("mode", "VIEW");
        intent.putExtra("computer_id", id);
        startActivity(intent);
    }
}