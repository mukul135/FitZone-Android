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
import com.fitzone.app.models.LoginData;
import com.fitzone.app.models.LoginRequest;
import com.fitzone.app.models.LoginResponse;
import com.fitzone.app.models.MemberInfo;
import com.fitzone.app.network.ApiService;
import com.fitzone.app.network.RetrofitClient;
import com.fitzone.app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

    private SessionManager sessionManager;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        ApiService apiService = RetrofitClient.getApiService(getApplicationContext());
        apiService.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);

                LoginResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess()
                        && body.getData() != null) {
                    handleLoginSuccess(body.getData());
                } else if (body != null && body.getMessage() != null) {
                    // Backend responded with a proper JSON error, e.g.
                    // "Invalid email or password" (INVALID_CREDENTIALS).
                    showError(body.getMessage());
                } else {
                    showError("Login failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                // Network unreachable, server down, timeout, malformed
                // response, etc. Never show the raw exception to the user.
                Log.e("LOGIN_DEBUG", "Network error", t);
                showError("Unable to connect to the server. Please check your internet connection and try again.");
            }
        });
    }

    private void handleLoginSuccess(LoginData data) {
        MemberInfo member = data.getMember();
        if (member == null) {
            showError("Unexpected server response. Please try again.");
            return;
        }

        sessionManager.saveSession(data.getToken(), member.getId(), member.getFullname());

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // Clears LoginActivity (and anything below it) off the back stack,
        // so pressing Back from Home never returns to Login.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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