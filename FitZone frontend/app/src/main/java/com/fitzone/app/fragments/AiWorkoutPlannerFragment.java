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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.LinkedHashMap;
import java.util.Map;

public class AiWorkoutPlannerFragment extends Fragment {

    private TextInputLayout tilGoal, tilDays, tilLevel;
    private AutoCompleteTextView actGoal, actLevel;
    private TextInputEditText etDays;
    private android.widget.LinearLayout containerResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_workout_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilGoal = view.findViewById(R.id.tilGoal);
        tilDays = view.findViewById(R.id.tilDays);
        tilLevel = view.findViewById(R.id.tilLevel);
        actGoal = view.findViewById(R.id.actGoal);
        actLevel = view.findViewById(R.id.actLevel);
        etDays = view.findViewById(R.id.etDays);
        containerResult = view.findViewById(R.id.containerResult);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.workout_goal_options, android.R.layout.simple_list_item_1);
        actGoal.setAdapter(goalAdapter);

        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.experience_level_options, android.R.layout.simple_list_item_1);
        actLevel.setAdapter(levelAdapter);

        view.findViewById(R.id.ivBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btnGenerate).setOnClickListener(v -> generateWorkout());
    }

    private void generateWorkout() {
        tilGoal.setError(null);
        tilDays.setError(null);
        tilLevel.setError(null);

        boolean isValid = true;

        String goal = actGoal.getText() != null ? actGoal.getText().toString().trim() : "";
        String levelStr = actLevel.getText() != null ? actLevel.getText().toString().trim() : "";
        String daysStr = etDays.getText() != null ? etDays.getText().toString().trim() : "";

        if (!goal.equals("Muscle Gain") && !goal.equals("Weight Loss") && !goal.equals("Strength")) {
            tilGoal.setError(getString(R.string.workout_error_goal));
            isValid = false;
        }

        if (!levelStr.equals("Beginner") && !levelStr.equals("Intermediate") && !levelStr.equals("Advanced")) {
            tilLevel.setError(getString(R.string.workout_error_level));
            isValid = false;
        }

        int days = 0;
        try {
            days = Integer.parseInt(daysStr);
            if (days <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilDays.setError(getString(R.string.workout_error_days));
            isValid = false;
        }

        if (!isValid) {
            containerResult.setVisibility(View.GONE);
            containerResult.removeAllViews();
            return;
        }

        Map<String, String> plan = generateWorkoutPlan(goal, days, levelStr);
        renderPlan(plan);
    }

    /**
     * Direct Java port of the original Flask generate_workout() function.
     * Exercise text, split logic, and volume notes are preserved exactly —
     * including the original's quirks (Muscle Gain split length ignores the
     * literal "days" value beyond bracket selection; Strength drops a
     * trailing odd day via integer division).
     */
    private Map<String, String> generateWorkoutPlan(String goal, int days, String level) {
        Map<String, String> plan = new LinkedHashMap<>();
        String[] split;
        Map<String, String> exercises = new LinkedHashMap<>();

        if (goal.equals("Muscle Gain")) {
            if (days >= 6) {
                split = new String[]{"Push", "Pull", "Legs", "Push", "Pull", "Legs"};
            } else if (days == 5) {
                split = new String[]{"Chest", "Back", "Legs", "Shoulders", "Arms"};
            } else {
                split = new String[]{"Upper Body", "Lower Body", "Rest", "Upper Body"};
            }

            exercises.put("Push", "Flat Bench Press 3x10,\nIncline Bench Press 3x10,Chest Flies 3x10,Shoulder Press 3x12,Lateral Raises 3x10,Tricep Pushdowns 3x12,French Curls 3x10");
            exercises.put("Pull", "Pull-ups 2x8, Barbell Rows 3x10,Lat Pulldown 3x10 , Bicep Curls 3x12, Hammer Curls 3x12");
            exercises.put("Legs", "Squats 4x8, Leg Press 3x12, Hamstring Curls 3x12,Leg Extensions 3x12, Calf Raises 2x15");
            exercises.put("Chest", "Incline Press 3x10, Chest Fly 3x12,Pushups 3x15");
            exercises.put("Back", "Lat Pulldown 4x10, Seated Row 3x12,Deadlifts 3x8");
            exercises.put("Shoulders", "Overhead Press 4x8, Lateral Raises 3x15,Rear Delt Fly 3x12");
            exercises.put("Arms", "Barbell Curl 3x12, Tricep Pushdown 3x12,Preacher Curl 3x10, Skull Crushers 3x10");
            exercises.put("Upper Body", "Bench Press, Rows, Shoulder Press (3x10 each)");
            exercises.put("Lower Body", "Squats, Lunges, Leg Curl (3x12 each)");

        } else if (goal.equals("Weight Loss")) {
            split = new String[days];
            for (int i = 0; i < days; i++) split[i] = "Full Body + Cardio";

            exercises.put("Full Body + Cardio", "Circuit Training 30 mins + 20 mins Treadmill + Core Workout");

        } else { // Strength
            int pairs = days / 2;
            split = new String[pairs * 2];
            for (int i = 0; i < pairs; i++) {
                split[i * 2] = "Heavy Upper";
                split[i * 2 + 1] = "Heavy Lower";
            }

            exercises.put("Heavy Upper", "Bench Press 5x5, Pull-ups 5x5, Overhead Press 5x5");
            exercises.put("Heavy Lower", "Squats 5x5, Deadlift 5x5, Leg Press 4x8");
        }

        String volumeNote = "";
        if (level.equals("Beginner")) {
            volumeNote = "Focus on form. Moderate weight.";
        } else if (level.equals("Intermediate")) {
            volumeNote = "Progressive overload recommended.";
        } else if (level.equals("Advanced")) {
            volumeNote = "High intensity. Add drop sets & supersets.";
        }

        for (int i = 0; i < split.length; i++) {
            String dayName = "Day " + (i + 1);
            String workoutType = split[i];

            if (exercises.containsKey(workoutType)) {
                plan.put(dayName, exercises.get(workoutType) + " | " + volumeNote);
            } else {
                plan.put(dayName, getString(R.string.workout_rest_day));
            }
        }

        return plan;
    }

    private void renderPlan(Map<String, String> plan) {
        containerResult.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Map.Entry<String, String> entry : plan.entrySet()) {
            View dayView = inflater.inflate(R.layout.item_workout_day, containerResult, false);

            TextView tvDayName = dayView.findViewById(R.id.tvDayName);
            TextView tvDayContent = dayView.findViewById(R.id.tvDayContent);

            tvDayName.setText(entry.getKey());
            tvDayContent.setText(entry.getValue());

            containerResult.addView(dayView);
        }

        containerResult.setVisibility(View.VISIBLE);
    }
}