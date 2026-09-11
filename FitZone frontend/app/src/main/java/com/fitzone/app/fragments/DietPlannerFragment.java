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

public class DietPlannerFragment extends Fragment {

    // {protein%, carbs%, fat%} in the same order as R.array.diet_goal_options
    private static final double[][] MACRO_SPLITS = {
            {0.40, 0.35, 0.25}, // Weight Loss
            {0.35, 0.45, 0.20}, // Muscle Gain
            {0.30, 0.40, 0.30}  // Maintain
    };

    private TextInputLayout tilCalories, tilGoal;

    private TextView tvMealBreakfast, tvMealLunch, tvMealSnack, tvMealDinner;
    private TextInputEditText etCalories;
    private AutoCompleteTextView actGoal;
    private MaterialCardView cardResult, cardMealStructure;
    private TextView tvTotalCalories, tvProtein, tvCarbs, tvFat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diet_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilCalories = view.findViewById(R.id.tilCalories);
        tilGoal = view.findViewById(R.id.tilGoal);
        etCalories = view.findViewById(R.id.etCalories);
        actGoal = view.findViewById(R.id.actGoal);

        cardResult = view.findViewById(R.id.cardResult);
        cardMealStructure = view.findViewById(R.id.cardMealStructure);
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvProtein = view.findViewById(R.id.tvProtein);
        tvCarbs = view.findViewById(R.id.tvCarbs);
        tvFat = view.findViewById(R.id.tvFat);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.diet_goal_options, android.R.layout.simple_list_item_1);
        actGoal.setAdapter(goalAdapter);

        view.findViewById(R.id.ivBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btnGenerate).setOnClickListener(v -> generatePlan());
        tvMealBreakfast = view.findViewById(R.id.tvMealBreakfast);
        tvMealLunch = view.findViewById(R.id.tvMealLunch);
        tvMealSnack = view.findViewById(R.id.tvMealSnack);
        tvMealDinner = view.findViewById(R.id.tvMealDinner);
    }
    /**
     * Static example meals per goal. NOT part of the original Flask app —
     * the original /diet route has no food-item data at all, only macro
     * numbers. This is new placeholder content, following the same static/
     * goal-keyed pattern as generate_workout()'s exercise dictionary.
     * No database, no API — fully local, matching the rest of this screen.
     */
    private void applyMealSuggestions(int goalIndex) {
        String breakfast, lunch, snack, dinner;

        if (goalIndex == 0) { // Weight Loss
            breakfast = "Oats with skim milk and berries";
            lunch = "Grilled chicken salad with olive oil dressing";
            snack = "Apple with a small handful of almonds";
            dinner = "Baked fish with steamed vegetables";
        } else if (goalIndex == 1) { // Muscle Gain
            breakfast = "Oats with milk, 2 boiled eggs, banana";
            lunch = "Grilled chicken, brown rice, mixed vegetables";
            snack = "Greek yogurt with almonds";
            dinner = "Baked fish, sweet potato, salad";
        } else { // Maintain
            breakfast = "Whole grain toast with eggs and avocado";
            lunch = "Chicken or paneer with rice and vegetables";
            snack = "Fruit and a handful of nuts";
            dinner = "Dal, roti, and a side salad";
        }

        tvMealBreakfast.setText(breakfast);
        tvMealLunch.setText(lunch);
        tvMealSnack.setText(snack);
        tvMealDinner.setText(dinner);
    }
    private void generatePlan() {
        tilCalories.setError(null);
        tilGoal.setError(null);

        boolean isValid = true;
        double calories = 0;

        String caloriesStr = etCalories.getText() != null ? etCalories.getText().toString().trim() : "";
        String goalStr = actGoal.getText() != null ? actGoal.getText().toString().trim() : "";

        try {
            calories = Double.parseDouble(caloriesStr);
            if (calories <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilCalories.setError(getString(R.string.diet_error_calories));
            isValid = false;
        }

        int goalIndex = indexOf(getResources().getStringArray(R.array.diet_goal_options), goalStr);
        if (goalIndex == -1) {
            tilGoal.setError(getString(R.string.diet_error_goal));
            isValid = false;
        }

        if (!isValid) {
            cardResult.setVisibility(View.GONE);
            cardMealStructure.setVisibility(View.GONE);
            return;
        }

        // Macro split by goal, preserved exactly from the original Flask app:
        // Weight Loss 40/35/25, Muscle Gain 35/45/20, Maintain 30/40/30 (P/C/F)
        double[] split = MACRO_SPLITS[goalIndex];
        double proteinPct = split[0];
        double carbsPct = split[1];
        double fatPct = split[2];

        // 4 kcal/g for protein & carbs, 9 kcal/g for fat
        double proteinGrams = (calories * proteinPct) / 4.0;
        double carbsGrams = (calories * carbsPct) / 4.0;
        double fatGrams = (calories * fatPct) / 9.0;

        tvTotalCalories.setText(String.format(Locale.US, "%.0f kcal", calories));
        tvProtein.setText(String.format(Locale.US, "%.0f g", proteinGrams));
        tvCarbs.setText(String.format(Locale.US, "%.0f g", carbsGrams));
        tvFat.setText(String.format(Locale.US, "%.0f g", fatGrams));

        applyMealSuggestions(goalIndex);
        cardResult.setVisibility(View.VISIBLE);
        cardMealStructure.setVisibility(View.VISIBLE);
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return -1;
    }
}