package com.example.assignment1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class ActivityBrands extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BrandAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brands);

        setTitle("Manage Brands");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler_view_brands);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadBrands();

        FloatingActionButton fab = findViewById(R.id.fab_add_brand);
        fab.setOnClickListener(v -> showAddBrandDialog());
    }

    private void loadBrands() {
        List<Brand> brands = dbHelper.getAllBrands();
        if (adapter == null) {
            adapter = new BrandAdapter(brands);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(brands);
        }
    }

    private void showAddBrandDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_brand, null);
        builder.setView(dialogView);

        final EditText etName = dialogView.findViewById(R.id.et_brand_name);
        final EditText etDesc = dialogView.findViewById(R.id.et_brand_desc);

        builder.setTitle("Add New Brand")
                .setPositiveButton("Add", (dialog, id) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();

                    if (!name.isEmpty()) {
                        long result = dbHelper.addBrand(name, desc);
                        if (result != -1) {
                            Toast.makeText(ActivityBrands.this, "Brand Added", Toast.LENGTH_SHORT).show();
                            loadBrands();
                        } else {
                            Toast.makeText(ActivityBrands.this, "Error adding brand", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ActivityBrands.this, "Name is required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
