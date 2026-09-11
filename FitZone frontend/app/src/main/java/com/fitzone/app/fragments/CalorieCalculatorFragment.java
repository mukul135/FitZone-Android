package com.fitzone.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fitzone.app.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class CalorieCalculatorFragment extends Fragment {

    // Activity multipliers, in the same order as R.array.activity_level_options
    private static final double[] ACTIVITY_MULTIPLIERS = {1.2, 1.375, 1.55, 1.725, 1.9};

    private TextInputLayout tilAge, tilWeight, tilHeight, tilGender, tilActivity, tilGoal;
    private TextInputEditText etAge, etWeight, etHeight;
    private AutoCompleteTextView actGender, actActivity, actGoal;
    private MaterialCardView cardResult;
    private TextView tvCalorieResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calorie_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilAge = view.findViewById(R.id.tilAge);
        tilWeight = view.findViewById(R.id.tilWeight);
        tilHeight = view.findViewById(R.id.tilHeight);
        tilGender = view.findViewById(R.id.tilGender);
        tilActivity = view.findViewById(R.id.tilActivity);
        tilGoal = view.findViewById(R.id.tilGoal);

        etAge = view.findViewById(R.id.etAge);
        etWeight = view.findViewById(R.id.etWeight);
        etHeight = view.findViewById(R.id.etHeight);

        actGender = view.findViewById(R.id.actGender);
        actActivity = view.findViewById(R.id.actActivity);
        actGoal = view.findViewById(R.id.actGoal);

        cardResult = view.findViewById(R.id.cardResult);
        tvCalorieResult = view.findViewById(R.id.tvCalorieResult);

        setupDropdown(actGender, R.array.gender_options);
        setupDropdown(actActivity, R.array.activity_level_options);
        setupDropdown(actGoal, R.array.calorie_goal_options);

        view.findViewById(R.id.ivBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btnCalculate).setOnClickListener(v -> calculateCalories());
    }

    private void setupDropdown(AutoCompleteTextView field, int arrayRes) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), arrayRes, android.R.layout.simple_list_item_1);
        field.setAdapter(adapter);
    }

    private void calculateCalories() {
        tilAge.setError(null);
        tilWeight.setError(null);
        tilHeight.setError(null);
        tilGender.setError(null);
        tilActivity.setError(null);
        tilGoal.setError(null);

        boolean isValid = true;

        double age = 0, weight = 0, height = 0;

        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String genderStr = actGender.getText() != null ? actGender.getText().toString().trim() : "";
        String activityStr = actActivity.getText() != null ? actActivity.getText().toString().trim() : "";
        String goalStr = actGoal.getText() != null ? actGoal.getText().toString().trim() : "";

        try {
            age = Double.parseDouble(ageStr);
            if (age <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilAge.setError(getString(R.string.calorie_error_age));
            isValid = false;
        }

        try {
            weight = Double.parseDouble(weightStr);
            if (weight <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilWeight.setError(getString(R.string.calorie_error_weight));
            isValid = false;
        }

        try {
            height = Double.parseDouble(heightStr);
            if (height <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilHeight.setError(getString(R.string.calorie_error_height));
            isValid = false;
        }

        int genderIndex = indexOf(getResources().getStringArray(R.array.gender_options), genderStr);
        if (genderIndex == -1) {
            tilGender.setError(getString(R.string.calorie_error_gender));
            isValid = false;
        }

        int activityIndex = indexOf(getResources().getStringArray(R.array.activity_level_options), activityStr);
        if (activityIndex == -1) {
            tilActivity.setError(getString(R.string.calorie_error_activity));
            isValid = false;
        }

        int goalIndex = indexOf(getResources().getStringArray(R.array.calorie_goal_options), goalStr);
        if (goalIndex == -1) {
            tilGoal.setError(getString(R.string.calorie_error_goal));
            isValid = false;
        }

        if (!isValid) {
            cardResult.setVisibility(View.GONE);
            return;
        }

        // Mifflin-St Jeor, preserved exactly from the original Flask app:
        // Male:   10W + 6.25H - 5A + 5
        // Female: 10W + 6.25H - 5A - 161
        double bmr;
        if (genderIndex == 0) { // Male
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else { // Female
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        double tdee = bmr * ACTIVITY_MULTIPLIERS[activityIndex];

        // Goal adjustment: Weight Loss -500, Muscle Gain +400, Maintain +0
        double goalAdjustment = 0;
        if (goalIndex == 0) goalAdjustment = -500;      // Weight Loss
        else if (goalIndex == 1) goalAdjustment = 400;  // Muscle Gain
        // Maintain = 0

        double finalCalories = tdee + goalAdjustment;

        String resultText = String.format(Locale.US, "%.0f %s",
                finalCalories, getString(R.string.calorie_result_unit));
        tvCalorieResult.setText(resultText);
        cardResult.setVisibility(View.VISIBLE);
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return -1;
    }
}