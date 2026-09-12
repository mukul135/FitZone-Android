package com.fitzone.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitzone.app.R;
import com.fitzone.app.activities.MainActivity;
import com.fitzone.app.adapters.ProgramAdapter;
import com.fitzone.app.models.MemberProfile;
import com.fitzone.app.models.ProfileResponse;
import com.fitzone.app.models.Program;
import com.fitzone.app.models.ProgramsResponse;
import com.fitzone.app.network.ApiService;
import com.fitzone.app.network.RetrofitClient;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvGoal, ivAvatar, tvPlan, tvWeight, tvBmi,
            tvFitnessGoal, tvSeeAll, tvErrorMessage;
    private MaterialCardView cardBmi, cardCalories;
    private Button btnViewMembership, btnRetry;
    private ProgressBar progressBarHome;
    private View layoutError, scrollContent;
    private RecyclerView rvPrograms;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupClickListeners();
        loadProfile();

        return view;
    }

    private void initializeViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvGoal = view.findViewById(R.id.tvGoal);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        tvPlan = view.findViewById(R.id.tvPlan);
        btnViewMembership = view.findViewById(R.id.btnViewMembership);

        tvWeight = view.findViewById(R.id.tvWeight);
        tvBmi = view.findViewById(R.id.tvBmi);
        tvFitnessGoal = view.findViewById(R.id.tvFitnessGoal);

        cardBmi = view.findViewById(R.id.cardBmi);
        cardCalories = view.findViewById(R.id.cardCalories);

        tvSeeAll = view.findViewById(R.id.tvSeeAll);
        rvPrograms = view.findViewById(R.id.rvPrograms);
        rvPrograms.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        progressBarHome = view.findViewById(R.id.progressBarHome);
        layoutError = view.findViewById(R.id.layoutError);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        btnRetry = view.findViewById(R.id.btnRetry);
        scrollContent = view.findViewById(R.id.scrollContent);
    }

    private void setupClickListeners() {
        ivAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProfileTab();
            }
        });

        btnViewMembership.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMembershipTab();
            }
        });

        cardBmi.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new BmiCalculatorFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardCalories.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new CalorieCalculatorFragment())
                    .addToBackStack(null)
                    .commit();
        });

        tvSeeAll.setOnClickListener(v -> startActivity(
                new Intent(requireContext(), com.fitzone.app.activities.ProgramsActivity.class)));

        btnRetry.setOnClickListener(v -> loadProfile());
    }

    private void loadProfile() {
        showLoading();

        ApiService apiService = RetrofitClient.getApiService(requireContext().getApplicationContext());
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call,
                                   @NonNull Response<ProfileResponse> response) {
                if (!isAdded()) return;

                if (response.code() == 401) {
                    // Token invalid/expired — same session-guard behavior as Phase 7.
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    displayProfile(response.body().getData());
                    loadPrograms();
                } else {
                    showError(getString(R.string.error_load_profile));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void displayProfile(MemberProfile member) {
        showContent();

        String fullname = (member.getFullname() != null) ? member.getFullname() : "Member";
        tvGreeting.setText(getString(R.string.home_greeting, fullname));

        String goal = (member.getGoal() != null && !member.getGoal().isEmpty())
                ? member.getGoal() : "Not set";
        tvGoal.setText(goal);
        tvFitnessGoal.setText(goal);

        ivAvatar.setText(getInitials(fullname));

        String plan = (member.getPlan() != null && !member.getPlan().isEmpty())
                ? member.getPlan() : "Not set";
        tvPlan.setText(plan);

        double weight = member.getWeight();
        double height = member.getHeight();

        tvWeight.setText(weight > 0
                ? String.format(Locale.getDefault(), "%.1f kg", weight)
                : "Not set");

        if (height > 0 && weight > 0) {
            double heightMeters = height / 100.0;
            double bmi = weight / (heightMeters * heightMeters);
            tvBmi.setText(String.format(Locale.getDefault(), "%.2f", bmi));
        } else {
            tvBmi.setText("Not set");
        }
    }

    private String getInitials(String fullname) {
        String[] parts = fullname.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty() && initials.length() < 2) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return initials.length() > 0 ? initials.toString() : "?";
    }

    private void loadPrograms() {
        ApiService apiService = RetrofitClient.getApiService(requireContext().getApplicationContext());
        apiService.getPrograms().enqueue(new Callback<ProgramsResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProgramsResponse> call,
                                   @NonNull Response<ProgramsResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() && response.body().getData() != null) {
                    displayPrograms(response.body().getData().getPrograms());
                }
                // Programs are secondary — profile data (already shown) matters more,
                // so we don't show an error banner if only this call fails.
            }

            @Override
            public void onFailure(@NonNull Call<ProgramsResponse> call, @NonNull Throwable t) {
                // Fail silently — same reasoning as above.
            }
        });
    }

    private void displayPrograms(List<Program> programs) {
        if (programs == null) return;
        rvPrograms.setAdapter(new ProgramAdapter(programs, program -> {
            Intent intent = new Intent(requireContext(), com.fitzone.app.activities.RegisterActivity.class);
            intent.putExtra(com.fitzone.app.activities.RegisterActivity.EXTRA_PROGRAM_NAME, program.getId());
            startActivity(intent);
        }));
    }

    private void showLoading() {
        progressBarHome.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBarHome.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBarHome.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }
}