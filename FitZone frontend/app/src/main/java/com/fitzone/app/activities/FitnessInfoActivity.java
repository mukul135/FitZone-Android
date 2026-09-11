package com.fitzone.app.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitzone.app.R;

public class FitnessInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvHeight = findViewById(R.id.tvHeight);
        TextView tvWeight = findViewById(R.id.tvWeight);
        TextView tvGoal = findViewById(R.id.tvGoal);
        TextView tvMedicalInfo = findViewById(R.id.tvMedicalInfo);

        double height = getIntent().getDoubleExtra("height", -1);
        double weight = getIntent().getDoubleExtra("weight", -1);

        tvHeight.setText(height > 0 ? (height + " cm") : getString(R.string.profile_not_available));
        tvWeight.setText(weight > 0 ? (weight + " kg") : getString(R.string.profile_not_available));
        tvGoal.setText(safe(getIntent().getStringExtra("goal")));
        tvMedicalInfo.setText(safe(getIntent().getStringExtra("medical_info")));
    }

    private String safe(String value) {
        return (value == null || value.trim().isEmpty())
                ? getString(R.string.profile_not_available)
                : value;
    }
}