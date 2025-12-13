package com.example.assignment1;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import java.util.List;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Activity2 extends AppCompatActivity implements ComputerAdapter.OnComputerClickListener {

    private RecyclerView recyclerView;
    private ComputerAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        setTitle(R.string.title_activity_computer_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.computer_recycler_view);
        emptyView = findViewById(R.id.empty_view);

        setupNavigation();

        FloatingActionButton fab = findViewById(R.id.fab_add_computer);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity2.this, Activity3.class);
                intent.putExtra("mode", "ADD");
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_manage_brands) {
            startActivity(new Intent(this, ActivityBrands.class));
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performSearch(String query) {
        List<Computer> computers;
        if (query.isEmpty()) {
            computers = dbHelper.getAllComputers();
        } else {
            computers = dbHelper.searchComputers(query);
        }
        updateList(computers);
    }

    private void updateList(List<Computer> computers) {
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
    protected void onResume() {
        super.onResume();
        loadComputers();
    }

    private void loadComputers() {
        List<Computer> computers = dbHelper.getAllComputers();
        updateList(computers);
    }

    @Override
    public void onComputerClick(long id) {
        Intent intent = new Intent(this, Activity3.class);
        intent.putExtra("mode", "VIEW");
        intent.putExtra("computer_id", id);
        startActivity(intent);
    }

    private void setupNavigation() {
        android.widget.ImageButton btnBack = findViewById(R.id.btn_nav_back);
        android.widget.ImageButton btnHome = findViewById(R.id.btn_nav_home);
        android.widget.ImageButton btnExit = findViewById(R.id.btn_nav_exit);

        btnBack.setOnClickListener(v -> finish());

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> finishAffinity());
    }
}
