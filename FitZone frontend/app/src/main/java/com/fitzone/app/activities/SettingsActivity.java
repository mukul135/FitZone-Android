package com.fitzone.app.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitzone.app.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> 
            startActivity(new android.content.Intent(this, ChangePasswordActivity.class))
        );
        
        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.settings_delete_account)
                .setMessage(R.string.settings_delete_account_confirm)
                .setPositiveButton("Delete", (dialog, which) -> {
                    android.widget.Toast.makeText(this, "Account deleted", android.widget.Toast.LENGTH_SHORT).show();
                    // Handle actual deletion logic here
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }
}