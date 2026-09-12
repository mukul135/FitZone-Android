package com.fitzone.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.fitzone.app.R;
import com.fitzone.app.fragments.HomeFragment;
import com.fitzone.app.fragments.MembershipFragment;
import com.fitzone.app.fragments.ProfileFragment;
import com.fitzone.app.fragments.ToolsFragment;
import com.fitzone.app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// ===============================
// MainActivity.java — Phase 8
// ===============================
// Now hosts the real 4-tab shell (Home | Tools | Membership | Profile)
// instead of the Phase 7 placeholder. The session guard from Phase 7 is
// preserved exactly. Logout moved into ProfileFragment (see Phase 8 notes) —
// this class exposes logout() so fragments can call it.

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());

        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavItemSelected);

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_tools) {
            selectedFragment = new ToolsFragment();
        } else if (itemId == R.id.nav_membership) {
            selectedFragment = new MembershipFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, selectedFragment)
                    .commit();
            return true;
        }
        return false;
    }

    // Called by HomeFragment's "View Membership" button.
    public void navigateToMembershipTab() {
        bottomNavigationView.setSelectedItemId(R.id.nav_membership);
    }

    // Called by HomeFragment's profile avatar.
    public void navigateToProfileTab() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
    }

    // Called by ProfileFragment's Logout button, and by HomeFragment on a
    // 401 from /api/profile (expired/invalid token).
    public void logout() {
        sessionManager.clearSession();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}