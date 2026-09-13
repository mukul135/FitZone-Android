package com.fitzone.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitzone.app.R;
import com.fitzone.app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

// ===============================
// LoginActivity.java
// ===============================
// WHAT THIS FILE DOES:
// The real Login screen (replaces the Step 3 placeholder). Validates
// input locally, calls POST /api/auth/login, and on success saves the
// session (SessionManager) and opens MainActivity. On failure, shows a
// clear inline/error message instead of crashing or showing raw JSON.

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private ProgressBar loadingSpinner;
    private TextView registerLink;

    private FirebaseAuth mAuth;
    private SessionManager sessionManager;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(getApplicationContext());

        emailLayout = findViewById(R.id.layoutEmail);
        passwordLayout = findViewById(R.id.layoutPassword);
        emailInput = findViewById(R.id.inputEmail);
        passwordInput = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.buttonLogin);
        loadingSpinner = findViewById(R.id.progressLogin);
        registerLink = findViewById(R.id.textRegisterLink);

        loginButton.setOnClickListener(v -> attemptLogin());

        registerLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        if (isLoading) {
            // Prevents double-submit while a request is already in flight.
            return;
        }

        // Clear any previous inline errors before re-validating.
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        }

        if (!isValid) {
            return; // Do not call the API if local validation fails.
        }

        setLoading(true);

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, fetch user details from Firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        fetchUserDetailsAndSaveSession(userId);
                    } else {
                        // If sign in fails, display a message to the user.
                        setLoading(false);
                        Log.w("LOGIN_DEBUG", "signInWithEmail:failure", task.getException());
                        showError("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void fetchUserDetailsAndSaveSession(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        String fullname = documentSnapshot.getString("fullname");
                        int internalId = 0; // We can't guarantee an integer ID with Firebase, so we just pass 0 for now. The backend shouldn't need it.
                        // Or you could change SessionManager to accept a String ID, but for now this works.
                        sessionManager.saveSession(userId, internalId, fullname != null ? fullname : "User");
                        
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        showError("User data not found in database.");
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e("LOGIN_DEBUG", "Error fetching user data", e);
                    showError("Error fetching user data.");
                    mAuth.signOut();
                });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        loadingSpinner.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "" : getString(R.string.login_button_text));
    }

    private void showError(String message) {
        // Shown as a Toast rather than under a specific field, since
        // invalid-credentials/network errors aren't tied to one input box.
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}