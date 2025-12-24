package com.example.assignment1;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Activity2 extends AppCompatActivity implements ComputerAdapter.OnComputerClickListener {

    private RecyclerView recyclerView;
    private ComputerAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView emptyView;
    private TextView tvCurrentDate;
    private Button btnFilterAll, btnFilterActive, btnFilterInactive, btnFilterDell;
    private TextView headerName, headerBrand, headerStatus;

    private List<Computer> allComputers;
    private String currentFilter = "ALL";
    private String currentSortColumn = "name";
    private boolean sortAscending = true;
    private Calendar currentCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        // Hide action bar for custom header
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new DatabaseHelper(this);
        currentCalendar = Calendar.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.computer_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        tvCurrentDate = findViewById(R.id.tv_current_date);

        // Filter buttons
        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilterActive = findViewById(R.id.btn_filter_active);
        btnFilterInactive = findViewById(R.id.btn_filter_inactive);
        btnFilterDell = findViewById(R.id.btn_filter_dell);

        // Column headers
        headerName = findViewById(R.id.header_name);
        headerBrand = findViewById(R.id.header_brand);
        headerStatus = findViewById(R.id.header_status);

        setupNavigation();
        setupDateNavigation();
        setupFilterButtons();
        setupColumnHeaders();
        updateDateDisplay();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab_add_computer);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(Activity2.this, Activity3.class);
            intent.putExtra("mode", "ADD");
            startActivity(intent);
        });

        // Menu button
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> {
            // Could open drawer or show menu
        });

        // Search button
        ImageButton btnSearch = findViewById(R.id.btn_search_header);
        btnSearch.setOnClickListener(v -> {
            // Could open search dialog
        });
    }

    private void setupDateNavigation() {
        ImageButton btnPrev = findViewById(R.id.btn_prev_date);
        ImageButton btnNext = findViewById(R.id.btn_next_date);

        btnPrev.setOnClickListener(v -> {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
        });

        btnNext.setOnClickListener(v -> {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
        });
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yy", Locale.ENGLISH);
        tvCurrentDate.setText(sdf.format(currentCalendar.getTime()));
    }

    private void setupFilterButtons() {
        btnFilterAll.setOnClickListener(v -> applyFilter("ALL"));
        btnFilterActive.setOnClickListener(v -> applyFilter("ACTIVE"));
        btnFilterInactive.setOnClickListener(v -> applyFilter("INACTIVE"));
        btnFilterDell.setOnClickListener(v -> applyFilter("DELL"));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        updateFilterButtonStyles();
        filterAndSortList();
    }

    private void updateFilterButtonStyles() {
        // Reset all buttons
        resetButtonStyle(btnFilterAll);
        resetButtonStyle(btnFilterActive);
        resetButtonStyle(btnFilterInactive);
        resetButtonStyle(btnFilterDell);

        // Highlight selected button
        Button selectedBtn = null;
        switch (currentFilter) {
            case "ALL":
                selectedBtn = btnFilterAll;
                break;
            case "ACTIVE":
                selectedBtn = btnFilterActive;
                break;
            case "INACTIVE":
                selectedBtn = btnFilterInactive;
                break;
            case "DELL":
                selectedBtn = btnFilterDell;
                break;
        }

        if (selectedBtn != null) {
            selectedBtn.setBackgroundColor(getResources().getColor(android.R.color.white, null));
            selectedBtn.setTextColor(getResources().getColor(R.color.teal_700, null));
        }
    }

    private void resetButtonStyle(Button btn) {
        btn.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
        btn.setTextColor(0xFFFFA726); // Orange color
    }

    private void setupColumnHeaders() {
        headerName.setOnClickListener(v -> sortByColumn("name"));
        headerBrand.setOnClickListener(v -> sortByColumn("brand"));
        headerStatus.setOnClickListener(v -> sortByColumn("status"));
    }

    private void sortByColumn(String column) {
        if (currentSortColumn.equals(column)) {
            sortAscending = !sortAscending;
        } else {
            currentSortColumn = column;
            sortAscending = true;
        }
        updateColumnHeaders();
        filterAndSortList();
    }

    private void updateColumnHeaders() {
        String arrow = sortAscending ? "↓" : "↑";
        headerName.setText(currentSortColumn.equals("name") ? "Name " + arrow : "Name ↕");
        headerBrand.setText(currentSortColumn.equals("brand") ? "Brand " + arrow : "Brand ↕");
        headerStatus.setText(currentSortColumn.equals("status") ? "Status " + arrow : "Status ↕");
    }

    private void filterAndSortList() {
        if (allComputers == null)
            return;

        List<Computer> filtered = new ArrayList<>();

        for (Computer computer : allComputers) {
            boolean matches = false;
            switch (currentFilter) {
                case "ALL":
                    matches = true;
                    break;
                case "ACTIVE":
                    matches = "Active".equalsIgnoreCase(computer.getStatus());
                    break;
                case "INACTIVE":
                    matches = "Inactive".equalsIgnoreCase(computer.getStatus());
                    break;
                case "DELL":
                    matches = computer.getBrandName() != null && computer.getBrandName().toLowerCase().contains("dell");
                    break;
            }
            if (matches)
                filtered.add(computer);
        }

        // Sort
        Collections.sort(filtered, getComparator());

        updateList(filtered);
    }

    private Comparator<Computer> getComparator() {
        Comparator<Computer> comparator;

        switch (currentSortColumn) {
            case "brand":
                comparator = (c1, c2) -> {
                    String b1 = c1.getBrandName() != null ? c1.getBrandName() : "";
                    String b2 = c2.getBrandName() != null ? c2.getBrandName() : "";
                    return b1.compareToIgnoreCase(b2);
                };
                break;
            case "status":
                comparator = (c1, c2) -> {
                    String s1 = c1.getStatus() != null ? c1.getStatus() : "";
                    String s2 = c2.getStatus() != null ? c2.getStatus() : "";
                    return s1.compareToIgnoreCase(s2);
                };
                break;
            default: // name
                comparator = (c1, c2) -> c1.getModel().compareToIgnoreCase(c2.getModel());
        }

        if (!sortAscending) {
            comparator = Collections.reverseOrder(comparator);
        }

        return comparator;
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
        allComputers = dbHelper.getAllComputers();
        filterAndSortList();
    }

    @Override
    public void onComputerClick(long id) {
        Intent intent = new Intent(this, Activity3.class);
        intent.putExtra("mode", "VIEW");
        intent.putExtra("computer_id", id);
        startActivity(intent);
    }

    private void setupNavigation() {
        ImageButton btnBack = findViewById(R.id.btn_nav_back);
        ImageButton btnHome = findViewById(R.id.btn_nav_home);
        ImageButton btnExit = findViewById(R.id.btn_nav_exit);

        btnBack.setOnClickListener(v -> finish());

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> finishAffinity());
    }
}
