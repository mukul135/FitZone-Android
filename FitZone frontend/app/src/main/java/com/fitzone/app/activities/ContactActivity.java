package com.fitzone.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitzone.app.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class ContactActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPhone, tilMessage;
    private TextInputEditText etName, etEmail, etPhone, etMessage;
    private Button btnSendMessage;
    private ProgressBar progressSend;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilMessage = findViewById(R.id.tilMessage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        progressSend = findViewById(R.id.progressSend);

        btnSendMessage.setOnClickListener(v -> attemptSubmit());
    }

    private void attemptSubmit() {
        if (isSubmitting) return; // prevent duplicate taps while a request is in flight

        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilMessage.setError(null);

        String name = safeText(etName);
        String email = safeText(etEmail);
        String phone = safeText(etPhone);
        String message = safeText(etMessage);

        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.contact_error_name));
            valid = false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.contact_error_email));
            valid = false;
        }
        if (TextUtils.isEmpty(message)) {
            tilMessage.setError(getString(R.string.contact_error_message));
            valid = false;
        }
        // Phone is optional, matching the original contact.html form (no "required" attribute on phone)

        if (!valid) return;

        submitContact(name, email, phone, message);
    }

    private void submitContact(String name, String email, String phone, String message) {
        setSubmitting(true);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("phone", phone);
        body.put("message", message);
        body.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("contact_messages").add(body)
                .addOnSuccessListener(documentReference -> {
                    setSubmitting(false);
                    Toast.makeText(ContactActivity.this,
                            R.string.contact_success, Toast.LENGTH_LONG).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    setSubmitting(false);
                    Toast.makeText(ContactActivity.this,
                            R.string.contact_error_server, Toast.LENGTH_LONG).show();
                });
    }

    private void clearForm() {
        etName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etMessage.setText("");
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        btnSendMessage.setEnabled(!submitting);
        progressSend.setVisibility(submitting ? View.VISIBLE : View.GONE);
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}