package com.fitzone.app.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.fitzone.app.models.Program;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvGoal, tvPlan, tvWeight, tvBmi,
            tvFitnessGoal, tvSeeAll, tvErrorMessage;
    private ImageView ivAvatar;
    private MaterialCardView cardBmi, cardCalories, cardDietPlan, cardWorkoutSchedule;
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
        cardDietPlan = view.findViewById(R.id.cardDietPlan);
        cardWorkoutSchedule = view.findViewById(R.id.cardWorkoutSchedule);

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

        if (cardDietPlan != null) {
            cardDietPlan.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new DietPlannerFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (cardWorkoutSchedule != null) {
            cardWorkoutSchedule.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new AiWorkoutPlannerFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        tvSeeAll.setOnClickListener(v -> startActivity(
                new Intent(requireContext(), com.fitzone.app.activities.ProgramsActivity.class)));

        btnRetry.setOnClickListener(v -> loadProfile());
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
                        displayProfile(documentSnapshot);
                        loadPrograms();
                    } else {
                        showError(getString(R.string.error_load_profile));
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    showError("Network error. Please check your connection.");
                });
    }

    private void displayProfile(DocumentSnapshot member) {
        showContent();

        String fullname = member.getString("fullname");
        if (fullname == null || fullname.isEmpty()) fullname = "Member";
        tvGreeting.setText(getString(R.string.home_greeting, fullname));

        String goal = member.getString("goal");
        if (goal == null || goal.isEmpty()) goal = "Not set";
        tvGoal.setText(goal);
        tvFitnessGoal.setText(goal);

        String profileImageBase64 = member.getString("profile_image");
        if (profileImageBase64 != null && profileImageBase64.startsWith("data:image")) {
            try {
                String cleanBase64 = profileImageBase64.substring(profileImageBase64.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivAvatar.setImageBitmap(bitmap);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ivAvatar.setImageResource(R.drawable.ic_profile);
            }
        } else {
            ivAvatar.setImageResource(R.drawable.ic_profile);
        }

        String plan = member.getString("plan");
        if (plan == null || plan.isEmpty()) plan = "Not set";
        tvPlan.setText(plan);

        Object weightObj = member.get("weight");
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
        
        Object heightObj = member.get("height");
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
        if (!isAdded()) return;
        List<Program> staticPrograms = new ArrayList<>();
        staticPrograms.add(new Program("Weight-Training", "Weight Training", "Build muscle and increase strength with structured weight lifting programs.", null));
        staticPrograms.add(new Program("Cardio-Training", "Cardio Training", "Improve stamina and heart health with treadmill, cycling, and HIIT workouts.", null));
        staticPrograms.add(new Program("Yoga-Flexibility", "Yoga & Flexibility", "Enhance flexibility, reduce stress, and improve mental focus.", null));
        staticPrograms.add(new Program("Personal-Training", "Personal Training", "One-on-one coaching tailored to your fitness goals.", null));
        staticPrograms.add(new Program("Muscle-Gain-Program", "Muscle Gain Program", "12-week structured strength training program focused on hypertrophy.", null));
        staticPrograms.add(new Program("Fat-Loss-Program", "Fat Loss Program", "8-week transformation plan combining HIIT, cardio, and diet guidance.", null));
        staticPrograms.add(new Program("Endurance-Training", "Endurance Training", "10-week advanced conditioning program to improve stamina and performance.", null));
        
        displayPrograms(staticPrograms);
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