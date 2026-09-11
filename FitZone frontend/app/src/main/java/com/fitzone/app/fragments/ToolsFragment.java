package com.fitzone.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fitzone.app.R;

public class ToolsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cardBmi).setOnClickListener(v -> openFragment(new BmiCalculatorFragment()));
        view.findViewById(R.id.cardCalorie).setOnClickListener(v -> openFragment(new CalorieCalculatorFragment()));
        view.findViewById(R.id.cardDiet).setOnClickListener(v -> openFragment(new DietPlannerFragment()));

        // AI Workout Planner — held back pending generate_workout() source
        view.findViewById(R.id.cardWorkout).setOnClickListener(v -> openFragment(new AiWorkoutPlannerFragment()));    }

    private void openFragment(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }


}