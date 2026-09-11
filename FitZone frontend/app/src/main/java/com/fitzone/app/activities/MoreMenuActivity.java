package com.fitzone.app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitzone.app.R;

public class MoreMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.rowMenuAbout).setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));
        findViewById(R.id.rowMenuGallery).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));
        findViewById(R.id.rowMenuPrograms).setOnClickListener(v ->
                startActivity(new Intent(this, ProgramsActivity.class)));
        findViewById(R.id.rowMenuContact).setOnClickListener(v ->
                startActivity(new Intent(this, ContactActivity.class)));
    }
}