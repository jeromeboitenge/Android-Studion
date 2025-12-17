package com.example.assignment1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class ActivityBrands extends AppCompatActivity {

    private TextView emptyView;
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private BrandAdapter adapter;

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
        emptyView = findViewById(R.id.tv_empty_brands);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadBrands();

        FloatingActionButton fab = findViewById(R.id.fab_add_brand);
        fab.setOnClickListener(v -> showAddBrandDialog());
    }

    private void loadBrands() {
        List<Brand> brands = dbHelper.getAllBrands();

        if (brands.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
            adapter = new BrandAdapter(brands, new BrandAdapter.OnBrandActionListener() {
                @Override
                public void onEdit(Brand brand) {
                    showBrandDialog(brand);
                }

                @Override
                public void onDelete(Brand brand) {
                    showDeleteConfirmation(brand);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(brands);
        }
    }

    // Reuse dialog for Add and Edit
    private void showBrandDialog(final Brand brandToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_brand, null);
        builder.setView(dialogView);

        final EditText etName = dialogView.findViewById(R.id.et_brand_name);
        final EditText etDesc = dialogView.findViewById(R.id.et_brand_desc);

        boolean isEdit = (brandToEdit != null);

        if (isEdit) {
            etName.setText(brandToEdit.getName());
            etDesc.setText(brandToEdit.getDescription());
            builder.setTitle("Edit Brand");
        } else {
            builder.setTitle("Add New Brand");
        }

        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, id) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (!name.isEmpty()) {
                if (isEdit) {
                    if (!name.equalsIgnoreCase(brandToEdit.getName()) && dbHelper.isBrandExists(name)) {
                        Toast.makeText(ActivityBrands.this, "Brand name already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        int result = dbHelper.updateBrand(brandToEdit.getId(), name, desc);
                        if (result > 0) {
                            Toast.makeText(ActivityBrands.this, "Brand Updated", Toast.LENGTH_SHORT).show();
                            loadBrands();
                        } else {
                            Toast.makeText(ActivityBrands.this, "Error updating brand", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (dbHelper.isBrandExists(name)) {
                        Toast.makeText(ActivityBrands.this, "Brand name already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        long result = dbHelper.addBrand(name, desc);
                        if (result != -1) {
                            Toast.makeText(ActivityBrands.this, "Brand Added", Toast.LENGTH_SHORT).show();
                            loadBrands();
                        } else {
                            Toast.makeText(ActivityBrands.this, "Error adding brand", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                Toast.makeText(ActivityBrands.this, "Name is required", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void showDeleteConfirmation(final Brand brand) {
        if (dbHelper.isBrandUsed(brand.getId())) {
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete Brand")
                    .setMessage("This brand is assigned to one or more computers. Please remove the computers first.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Brand")
                    .setMessage("Are you sure you want to delete " + brand.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbHelper.deleteBrand(brand.getId());
                        Toast.makeText(ActivityBrands.this, "Brand Deleted", Toast.LENGTH_SHORT).show();
                        loadBrands();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void showAddBrandDialog() {
        showBrandDialog(null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
