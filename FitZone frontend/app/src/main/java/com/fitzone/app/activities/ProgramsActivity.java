package com.fitzone.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitzone.app.R;
import com.fitzone.app.adapters.ProgramFullAdapter;
import com.fitzone.app.models.Program;

import java.util.ArrayList;
import java.util.List;
import android.widget.Button;

// ===============================
// ProgramsActivity.java
// ===============================
// WHAT THIS FILE DOES:
// Full vertical list of all 7 FitZone programs (GET /api/programs, the
// same endpoint HomeFragment already uses for its preview row). Each
// card's Register button launches RegisterActivity with the program's
// id, using the exact same EXTRA_PROGRAM_NAME mechanism RegisterActivity
// has supported since Phase 7 — no changes to RegisterActivity needed.

public class ProgramsActivity extends AppCompatActivity {

    private ProgressBar progressBarPrograms;
    private RecyclerView rvProgramsFull;
    private View layoutError;
    private TextView tvErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programs);

        bindViews();
        loadPrograms();
    }

    private void bindViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        progressBarPrograms = findViewById(R.id.progressBarPrograms);
        rvProgramsFull = findViewById(R.id.rvProgramsFull);
        rvProgramsFull.setLayoutManager(new LinearLayoutManager(this));

        layoutError = findViewById(R.id.layoutError);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> loadPrograms());
    }

    private void loadPrograms() {
        showLoading();

        List<Program> staticPrograms = new ArrayList<>();
        staticPrograms.add(new Program("Weight-Training", "Weight Training", "Build muscle and increase strength with structured weight lifting programs.", null));
        staticPrograms.add(new Program("Cardio-Training", "Cardio Training", "Improve stamina and heart health with treadmill, cycling, and HIIT workouts.", null));
        staticPrograms.add(new Program("Yoga-Flexibility", "Yoga & Flexibility", "Enhance flexibility, reduce stress, and improve mental focus.", null));
        staticPrograms.add(new Program("Personal-Training", "Personal Training", "One-on-one coaching tailored to your fitness goals.", null));
        staticPrograms.add(new Program("Muscle-Gain-Program", "Muscle Gain Program", "12-week structured strength training program focused on hypertrophy.", null));
        staticPrograms.add(new Program("Fat-Loss-Program", "Fat Loss Program", "8-week transformation plan combining HIIT, cardio, and diet guidance.", null));
        staticPrograms.add(new Program("Endurance-Training", "Endurance Training", "10-week advanced conditioning program to improve stamina and performance.", null));
        
        showContent(staticPrograms);
    }

    private void showLoading() {
        progressBarPrograms.setVisibility(View.VISIBLE);
        rvProgramsFull.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void showContent(List<Program> programs) {
        progressBarPrograms.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        rvProgramsFull.setVisibility(View.VISIBLE);

        rvProgramsFull.setAdapter(new ProgramFullAdapter(programs, program -> {
            Intent intent = new Intent(ProgramsActivity.this, RegisterActivity.class);
            intent.putExtra(RegisterActivity.EXTRA_PROGRAM_NAME, program.getId());
            startActivity(intent);
        }));
    }

    private void showError(String message) {
        progressBarPrograms.setVisibility(View.GONE);
        rvProgramsFull.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }
}