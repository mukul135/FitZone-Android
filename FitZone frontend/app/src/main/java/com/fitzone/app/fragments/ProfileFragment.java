package com.fitzone.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fitzone.app.R;
import com.fitzone.app.activities.FitnessInfoActivity;
import com.fitzone.app.activities.HelpSupportActivity;
import com.fitzone.app.activities.MainActivity;
import com.fitzone.app.activities.PersonalInfoActivity;
import com.fitzone.app.activities.SettingsActivity;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ScrollView scrollContent;
    private ProgressBar progressBar;
    private LinearLayout errorGroup;
    private Button btnRetry;

    private TextView tvName, tvEmail;
    private ImageView ivProfileImage;

    private DocumentSnapshot currentProfileDoc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        scrollContent = view.findViewById(R.id.scrollContent);
        progressBar = view.findViewById(R.id.progressBar);
        errorGroup = view.findViewById(R.id.errorGroup);
        btnRetry = view.findViewById(R.id.btnRetry);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);

        btnRetry.setOnClickListener(v -> loadProfile());

        view.findViewById(R.id.cardPersonalInfo).setOnClickListener(v -> openPersonalInfo());
        view.findViewById(R.id.cardFitnessInfo).setOnClickListener(v -> openFitnessInfo());
        view.findViewById(R.id.cardMembership).setOnClickListener(v -> goToMembership());
        view.findViewById(R.id.cardSettings).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SettingsActivity.class)));
        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SettingsActivity.class)));
        view.findViewById(R.id.cardHelpSupport).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), HelpSupportActivity.class)));
        view.findViewById(R.id.cardMore).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), com.fitzone.app.activities.MoreMenuActivity.class)));
        view.findViewById(R.id.btnSendMessage).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), HelpSupportActivity.class)));
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
        });

        loadProfile();

        return view;
    }

    private void loadProfile() {
        showLoading();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    if (documentSnapshot.exists()) {
                        currentProfileDoc = documentSnapshot;
                        populateProfile(currentProfileDoc);
                        showContent();
                    } else {
                        showError();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    showError();
                });
    }

    private void populateProfile(DocumentSnapshot profile) {
        String fullname = safe(profile.getString("fullname"));
        tvName.setText(fullname);
        tvEmail.setText(safe(profile.getString("email")));

        String profileImageBase64 = profile.getString("profile_image");
        if (profileImageBase64 != null && profileImageBase64.startsWith("data:image")) {
            try {
                String cleanBase64 = profileImageBase64.substring(profileImageBase64.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap);
                } else {
                    ivProfileImage.setImageResource(R.drawable.ic_profile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ivProfileImage.setImageResource(R.drawable.ic_profile);
            }
        } else {
            ivProfileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    // Same formula used on Home: weight / (height_m^2), height provided in cm.
    private String calculateBmiDisplay(double heightCm, double weightKg) {
        if (heightCm <= 0 || weightKg <= 0) {
            return "--";
        }
        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);
        return String.format("%.2f", bmi);
    }

    private String getInitials(String fullname) {
        if (fullname == null || fullname.trim().isEmpty()) return "?";
        String[] parts = fullname.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        initials.append(Character.toUpperCase(parts[0].charAt(0)));
        if (parts.length > 1) {
            initials.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        return initials.toString();
    }

    private String safe(String value) {
        return (value == null || value.trim().isEmpty())
                ? getString(R.string.profile_not_available)
                : value;
    }

    private void openPersonalInfo() {
        if (currentProfileDoc == null || getActivity() == null) return;
        Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
        intent.putExtra("fullname", currentProfileDoc.getString("fullname"));
        intent.putExtra("email", currentProfileDoc.getString("email"));
        intent.putExtra("mobile", currentProfileDoc.getString("phone"));
        intent.putExtra("dob", currentProfileDoc.getString("dob"));
        intent.putExtra("gender", currentProfileDoc.getString("gender"));
        startActivity(intent);
    }

    private void openFitnessInfo() {
        if (currentProfileDoc == null || getActivity() == null) return;
        Intent intent = new Intent(getActivity(), FitnessInfoActivity.class);
        
        Object heightObj = currentProfileDoc.get("height");
        double height = 0.0;
        if (heightObj instanceof Number) {
            height = ((Number) heightObj).doubleValue();
        } else if (heightObj instanceof String) {
            try {
                height = Double.parseDouble((String) heightObj);
            } catch (NumberFormatException e) {
                height = 0.0;
            }
        }
        intent.putExtra("height", height);
        
        Object weightObj = currentProfileDoc.get("weight");
        double weight = 0.0;
        if (weightObj instanceof Number) {
            weight = ((Number) weightObj).doubleValue();
        } else if (weightObj instanceof String) {
            try {
                weight = Double.parseDouble((String) weightObj);
            } catch (NumberFormatException e) {
                weight = 0.0;
            }
        }
        intent.putExtra("weight", weight);
        
        intent.putExtra("goal", currentProfileDoc.getString("goal"));
        intent.putExtra("medical_info", currentProfileDoc.getString("medicalInfo"));
        startActivity(intent);
    }

    private void goToMembership() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToMembershipTab();
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        errorGroup.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
        errorGroup.setVisibility(View.GONE);
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        errorGroup.setVisibility(View.VISIBLE);
    }
}