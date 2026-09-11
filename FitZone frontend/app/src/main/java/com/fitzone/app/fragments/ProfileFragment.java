package com.fitzone.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.fitzone.app.models.MemberProfile;
import com.fitzone.app.models.ProfileResponse;
import com.fitzone.app.network.ApiService;
import com.fitzone.app.network.RetrofitClient;
import com.google.android.material.chip.Chip;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ScrollView scrollContent;
    private ProgressBar progressBar;
    private LinearLayout errorGroup;
    private Button btnRetry;

    private TextView tvAvatarInitials, tvName, tvEmail, tvBmiValue;
    private Chip chipGoal;

    private MemberProfile currentProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        scrollContent = view.findViewById(R.id.scrollContent);
        progressBar = view.findViewById(R.id.progressBar);
        errorGroup = view.findViewById(R.id.errorGroup);
        btnRetry = view.findViewById(R.id.btnRetry);

        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        chipGoal = view.findViewById(R.id.chipGoal);
        tvBmiValue = view.findViewById(R.id.tvBmiValue);

        btnRetry.setOnClickListener(v -> loadProfile());

        view.findViewById(R.id.rowPersonalInfo).setOnClickListener(v -> openPersonalInfo());
        view.findViewById(R.id.rowFitnessInfo).setOnClickListener(v -> openFitnessInfo());
        view.findViewById(R.id.rowMembership).setOnClickListener(v -> goToMembership());
        view.findViewById(R.id.rowSettings).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SettingsActivity.class)));
        view.findViewById(R.id.rowHelpSupport).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), HelpSupportActivity.class)));
        view.findViewById(R.id.rowMore).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), com.fitzone.app.activities.MoreMenuActivity.class)));
        view.findViewById(R.id.rowLogout).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
        });

        loadProfile();

        return view;
    }

    private void loadProfile() {
        showLoading();

        ApiService api = RetrofitClient.getApiService(requireContext().getApplicationContext());
        api.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call,
                                   @NonNull Response<ProfileResponse> response) {
                if (!isAdded()) return;

                if (response.code() == 401) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                    return;
                }

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() && response.body().getData() != null) {
                    currentProfile = response.body().getData();
                    populateProfile(currentProfile);
                    showContent();
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showError();
            }
        });
    }

    private void populateProfile(MemberProfile profile) {
        String fullname = safe(profile.getFullname());
        tvName.setText(fullname);
        tvEmail.setText(safe(profile.getEmail()));
        tvAvatarInitials.setText(getInitials(fullname));

        String goal = profile.getGoal();
        if (goal != null && !goal.trim().isEmpty()) {
            chipGoal.setVisibility(View.VISIBLE);
            chipGoal.setText(goal);
        } else {
            chipGoal.setVisibility(View.GONE);
        }

        tvBmiValue.setText(calculateBmiDisplay(profile.getHeight(), profile.getWeight()));
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
        if (currentProfile == null || getActivity() == null) return;
        Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
        intent.putExtra("fullname", currentProfile.getFullname());
        intent.putExtra("email", currentProfile.getEmail());
        intent.putExtra("mobile", currentProfile.getMobile());
        intent.putExtra("dob", currentProfile.getDob());
        intent.putExtra("gender", currentProfile.getGender());
        startActivity(intent);
    }

    private void openFitnessInfo() {
        if (currentProfile == null || getActivity() == null) return;
        Intent intent = new Intent(getActivity(), FitnessInfoActivity.class);
        intent.putExtra("height", currentProfile.getHeight());
        intent.putExtra("weight", currentProfile.getWeight());
        intent.putExtra("goal", currentProfile.getGoal());
        intent.putExtra("medical_info", currentProfile.getMedicalInfo());
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