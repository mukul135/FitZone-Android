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
import com.fitzone.app.models.ProgramsResponse;
import com.fitzone.app.network.ApiService;
import com.fitzone.app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

        ApiService apiService = RetrofitClient.getApiService(getApplicationContext());
        apiService.getPrograms().enqueue(new Callback<ProgramsResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProgramsResponse> call,
                                   @NonNull Response<ProgramsResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() && response.body().getData() != null) {
                    List<Program> programs = response.body().getData().getPrograms();
                    if (programs == null || programs.isEmpty()) {
                        showError(getString(R.string.error_no_programs));
                    } else {
                        showContent(programs);
                    }
                } else {
                    showError(getString(R.string.error_load_programs));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProgramsResponse> call, @NonNull Throwable t) {
                showError("Network error. Please check your connection.");
            }
        });
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