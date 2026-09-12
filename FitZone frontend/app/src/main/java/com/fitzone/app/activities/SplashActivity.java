package com.fitzone.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.fitzone.app.R;
import com.fitzone.app.utils.SessionManager;

// ===============================
// SplashActivity.java
// ===============================
// WHAT THIS FILE DOES:
// This is now the app's launcher activity (replacing DesignPreviewActivity,
// which was only ever meant to be temporary — see Phase 6 report, Section 7
// "Known Open Item").
//
// It checks whether the user is already logged in (via SessionManager,
// wired up in Phase 7 Step 2) and routes accordingly:
//   - logged in      -> MainActivity (Home)
//   - not logged in  -> LoginActivity
//
// No onboarding check yet — confirmed skipped for Phase 7 (no
// OnboardingActivity exists in the project yet). If onboarding is added
// later, its check would go here, before the login check.
//
// A short delay is used purely so the splash screen is visible for a beat
// instead of flashing instantly — this is a UX nicety, not a functional
// requirement, and can be removed/shortened if you don't want it.

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find views
        android.view.View content = findViewById(R.id.splashContent);
        android.view.View bottomText = findViewById(R.id.splashBottomText);

        // Initial state for animation (start slightly lower)
        content.setTranslationY(100f);
        bottomText.setTranslationY(50f);

        // Animate content (fade in and slide up)
        content.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        // Animate bottom text (fade in and slide up slightly later)
        bottomText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(800)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(this::routeNextScreen, SPLASH_DELAY_MS);
    }

    private void routeNextScreen() {
        SessionManager sessionManager = new SessionManager(getApplicationContext());

        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        // Clear SplashActivity from the back stack so pressing Back never
        // returns to it.
        startActivity(intent);
        finish();
    }
}