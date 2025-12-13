package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Activity3 extends AppCompatActivity implements FragmentDetails.OnComputerActionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        if (savedInstanceState == null) {
            handleIntent();
        }

        setupNavigation();
    }

    private void handleIntent() {
        String mode = getIntent().getStringExtra("mode");
        long computerId = getIntent().getLongExtra("computer_id", -1);

        if ("VIEW".equals(mode) && computerId != -1) {
            setTitle(getString(R.string.title_details));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, FragmentDetails.newInstance(computerId))
                    .commit();
        } else {
            // Default to ADD mode
            setTitle(getString(R.string.title_activity_add_computer));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Fragment1())
                    .commit();
        }
    }

    @Override
    public void onEditComputer(long id) {
        setTitle(getString(R.string.btn_edit));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, Fragment1.newInstance(id))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeleteComputer() {
        finish();
    }

    private void setupNavigation() {
        android.widget.ImageButton btnBack = findViewById(R.id.btn_nav_back);
        android.widget.ImageButton btnHome = findViewById(R.id.btn_nav_home);
        android.widget.ImageButton btnExit = findViewById(R.id.btn_nav_exit);

        btnBack.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> finishAffinity());
    }
}
