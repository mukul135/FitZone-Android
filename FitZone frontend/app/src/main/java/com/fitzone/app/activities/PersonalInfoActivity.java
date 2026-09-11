package com.fitzone.app.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitzone.app.R;

public class PersonalInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvFullName = findViewById(R.id.tvFullName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvMobile = findViewById(R.id.tvMobile);
        TextView tvDob = findViewById(R.id.tvDob);
        TextView tvGender = findViewById(R.id.tvGender);

        tvFullName.setText(safe(getIntent().getStringExtra("fullname")));
        tvEmail.setText(safe(getIntent().getStringExtra("email")));
        tvMobile.setText(safe(getIntent().getStringExtra("mobile")));
        tvDob.setText(safe(getIntent().getStringExtra("dob")));
        tvGender.setText(safe(getIntent().getStringExtra("gender")));
    }

    private String safe(String value) {
        return (value == null || value.trim().isEmpty())
                ? getString(R.string.profile_not_available)
                : value;
    }
}