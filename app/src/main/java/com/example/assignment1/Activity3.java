```java
package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Activity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        setTitle(R.string.title_activity_add_computer);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Fragment1())
                    .commit();
        }

        setupNavigation();
    }

    private void setupNavigation() {
        android.widget.Button btnBack = findViewById(R.id.btn_nav_back);
        android.widget.Button btnHome = findViewById(R.id.btn_nav_home);
        android.widget.Button btnExit = findViewById(R.id.btn_nav_exit);

        btnBack.setOnClickListener(v -> finish());

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> finishAffinity());
    }
}
