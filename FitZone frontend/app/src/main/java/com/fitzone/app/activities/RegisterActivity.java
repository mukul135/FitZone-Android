package com.fitzone.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitzone.app.R;
import com.fitzone.app.models.RegisterRequest;
import com.fitzone.app.models.RegisterResponse;
import com.fitzone.app.network.ApiService;
import com.fitzone.app.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ===============================
// RegisterActivity.java
// ===============================
// WHAT THIS FILE DOES:
// The real Registration screen (replaces the Step 4 placeholder).
// Matches api_register()'s required_fields exactly:
//   fullname, email, mobile, password, confirm_password, dob, gender,
//   height, weight, goal, plan, medical_info, emergency_name, emergency_number
//
// PROGRAM-SPECIFIC REGISTRATION (Phase 1 Decision 2):
// If launched with an Intent extra "program_name" (a value matching
// Flask's VALID_PROGRAM_IDS, e.g. "Weight-Training"), this screen shows a
// "Registering for: ..." banner and submits to
// POST /api/register/<program_name> instead of POST /api/auth/register.
// Nothing currently launches RegisterActivity with that extra yet — the
// screen that would do that (ProgramsActivity) hasn't been built. This is
// the same situation LoginActivity was in for MainActivity before Step 3.

public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_PROGRAM_NAME = "program_name";

    private TextView programBanner;

    private TextInputLayout layoutFullName, layoutEmail, layoutMobile, layoutPassword,
            layoutConfirmPassword, layoutDob, layoutHeight, layoutWeight,
            layoutEmergencyName, layoutEmergencyNumber, layoutMedicalInfo;
    private TextInputEditText inputFullName, inputEmail, inputMobile, inputPassword,
            inputConfirmPassword, inputDob, inputHeight, inputWeight,
            inputEmergencyName, inputEmergencyNumber, inputMedicalInfo;

    private RadioGroup radioGender, radioPlan;
    private TextView errorGender, errorPlan, errorTerms;

    private AutoCompleteTextView dropdownGoal;

    private CheckBox checkboxTerms;
    private Button registerButton;
    private ProgressBar loadingSpinner;

    private String programName; // null if this is a plain (non-program) registration
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        programName = getIntent().getStringExtra(EXTRA_PROGRAM_NAME);

        bindViews();
        setupGoalDropdown();
        setupDobPicker();
        setupProgramBanner();

        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void bindViews() {
        programBanner = findViewById(R.id.textProgramBanner);

        layoutFullName = findViewById(R.id.layoutFullName);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutMobile = findViewById(R.id.layoutMobile);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        layoutDob = findViewById(R.id.layoutDob);
        layoutHeight = findViewById(R.id.layoutHeight);
        layoutWeight = findViewById(R.id.layoutWeight);
        layoutEmergencyName = findViewById(R.id.layoutEmergencyName);
        layoutEmergencyNumber = findViewById(R.id.layoutEmergencyNumber);
        layoutMedicalInfo = findViewById(R.id.layoutMedicalInfo);

        inputFullName = findViewById(R.id.inputFullName);
        inputEmail = findViewById(R.id.inputEmail);
        inputMobile = findViewById(R.id.inputMobile);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        inputDob = findViewById(R.id.inputDob);
        inputHeight = findViewById(R.id.inputHeight);
        inputWeight = findViewById(R.id.inputWeight);
        inputEmergencyName = findViewById(R.id.inputEmergencyName);
        inputEmergencyNumber = findViewById(R.id.inputEmergencyNumber);
        inputMedicalInfo = findViewById(R.id.inputMedicalInfo);

        radioGender = findViewById(R.id.radioGender);
        radioPlan = findViewById(R.id.radioPlan);
        errorGender = findViewById(R.id.textErrorGender);
        errorPlan = findViewById(R.id.textErrorPlan);
        errorTerms = findViewById(R.id.textErrorTerms);

        dropdownGoal = findViewById(R.id.dropdownGoal);

        checkboxTerms = findViewById(R.id.checkboxTerms);
        registerButton = findViewById(R.id.buttonRegister);
        loadingSpinner = findViewById(R.id.progressRegister);
    }

    private void setupGoalDropdown() {
        android.widget.ArrayAdapter<CharSequence> adapter = android.widget.ArrayAdapter.createFromResource(
                this, R.array.goal_options, android.R.layout.simple_list_item_1);
        dropdownGoal.setAdapter(adapter);
    }

    private void setupDobPicker() {
        inputDob.setFocusable(false);
        inputDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    RegisterActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        // Stored/sent as yyyy-MM-dd, a standard SQL-friendly date format
                        String date = String.format(java.util.Locale.US, "%04d-%02d-%02d",
                                year, month + 1, dayOfMonth);
                        inputDob.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    private void setupProgramBanner() {
        if (!TextUtils.isEmpty(programName)) {
            // Simple readability transform only — no new data invented,
            // just formatting the existing program id (e.g. "Weight-Training").
            String displayName = programName.replace("-", " ");
            programBanner.setText("Registering for: " + displayName);
            programBanner.setVisibility(View.VISIBLE);
        } else {
            programBanner.setVisibility(View.GONE);
        }
    }

    private void attemptRegister() {
        if (isLoading) {
            return;
        }

        clearErrors();

        String fullName = textOf(inputFullName);
        String email = textOf(inputEmail);
        String mobile = textOf(inputMobile);
        String password = textOf(inputPassword);
        String confirmPassword = textOf(inputConfirmPassword);
        String dob = textOf(inputDob);
        String height = textOf(inputHeight);
        String weight = textOf(inputWeight);
        String goal = dropdownGoal.getText() != null ? dropdownGoal.getText().toString().trim() : "";
        String medicalInfo = textOf(inputMedicalInfo);
        String emergencyName = textOf(inputEmergencyName);
        String emergencyNumber = textOf(inputEmergencyNumber);

        String gender = selectedRadioText(radioGender);
        String plan = selectedRadioText(radioPlan);

        boolean isValid = true;

        if (TextUtils.isEmpty(fullName)) {
            layoutFullName.setError("Full name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Enter a valid email address");
            isValid = false;
        }
        if (TextUtils.isEmpty(mobile)) {
            layoutMobile.setError("Mobile number is required");
            isValid = false;
        } else if (!Patterns.PHONE.matcher(mobile).matches()) {
            layoutMobile.setError("Enter a valid mobile number");
            isValid = false;
        }
        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError("Password is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            layoutConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            layoutConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }
        if (TextUtils.isEmpty(dob)) {
            layoutDob.setError("Date of birth is required");
            isValid = false;
        }
        if (gender == null) {
            errorGender.setText("Please select a gender");
            errorGender.setVisibility(View.VISIBLE);
            isValid = false;
        }
        if (TextUtils.isEmpty(height)) {
            layoutHeight.setError("Height is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(weight)) {
            layoutWeight.setError("Weight is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(goal)) {
            // Shown via Toast since AutoCompleteTextView isn't wrapped for
            // setError the same way; still validated before the API call.
            Toast.makeText(this, "Please select a fitness goal", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (plan == null) {
            errorPlan.setText("Please select a membership plan");
            errorPlan.setVisibility(View.VISIBLE);
            isValid = false;
        }
        if (TextUtils.isEmpty(medicalInfo)) {
            // Required because api_register()'s required_fields includes
            // medical_info — matching real backend behavior, not inventing it.
            layoutMedicalInfo.setError("Medical information is required (enter \"None\" if not applicable)");
            isValid = false;
        }
        if (TextUtils.isEmpty(emergencyName)) {
            layoutEmergencyName.setError("Emergency contact name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(emergencyNumber)) {
            layoutEmergencyNumber.setError("Emergency contact number is required");
            isValid = false;
        }
        if (!checkboxTerms.isChecked()) {
            errorTerms.setText("You must accept the Terms & Conditions to register");
            errorTerms.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        RegisterRequest request = new RegisterRequest(
                fullName, email, mobile, password, confirmPassword, dob, gender,
                height, weight, goal, plan, medicalInfo, emergencyName, emergencyNumber
        );

        setLoading(true);

        ApiService apiService = RetrofitClient.getApiService(getApplicationContext());
        Call<RegisterResponse> call = TextUtils.isEmpty(programName)
                ? apiService.register(request)
                : apiService.registerForProgram(programName, request);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                setLoading(false);
                RegisterResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful. Please log in.", Toast.LENGTH_LONG).show();
                    // Matches the original app's behavior: redirect to Login
                    // after registration, do not auto-login (Phase 7 rule #19).
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else if (body != null && "DUPLICATE_EMAIL".equals(body.getError())) {
                    layoutEmail.setError("An account with this email already exists");
                    Toast.makeText(RegisterActivity.this,
                            "An account with this email already exists. Please log in instead.",
                            Toast.LENGTH_LONG).show();
                } else if (body != null && body.getMessage() != null) {
                    Toast.makeText(RegisterActivity.this, body.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Registration failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Unable to connect to the server. Please check your internet connection and try again.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearErrors() {
        layoutFullName.setError(null);
        layoutEmail.setError(null);
        layoutMobile.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
        layoutDob.setError(null);
        layoutHeight.setError(null);
        layoutWeight.setError(null);
        layoutMedicalInfo.setError(null);
        layoutEmergencyName.setError(null);
        layoutEmergencyNumber.setError(null);
        errorGender.setVisibility(View.GONE);
        errorPlan.setVisibility(View.GONE);
        errorTerms.setVisibility(View.GONE);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    // Returns the text of the selected RadioButton, or null if none selected.
    private String selectedRadioText(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return null;
        }
        android.widget.RadioButton selected = findViewById(selectedId);
        return selected.getText().toString();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        loadingSpinner.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
        registerButton.setText(loading ? "" : getString(R.string.register_button_text));
    }
}