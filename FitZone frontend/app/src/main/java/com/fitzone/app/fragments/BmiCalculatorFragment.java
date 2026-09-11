package com.fitzone.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fitzone.app.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class BmiCalculatorFragment extends Fragment {

    private TextInputLayout tilHeight, tilWeight;
    private TextInputEditText etHeight, etWeight;
    private MaterialCardView cardResult;
    private android.widget.TextView tvBmiResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bmi_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilHeight = view.findViewById(R.id.tilHeight);
        tilWeight = view.findViewById(R.id.tilWeight);
        etHeight = view.findViewById(R.id.etHeight);
        etWeight = view.findViewById(R.id.etWeight);
        cardResult = view.findViewById(R.id.cardResult);
        tvBmiResult = view.findViewById(R.id.tvBmiResult);

        view.findViewById(R.id.ivBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btnCalculate).setOnClickListener(v -> calculateBmi());
    }

    private void calculateBmi() {
        tilHeight.setError(null);
        tilWeight.setError(null);

        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";

        boolean isValid = true;
        double heightCm = 0;
        double weightKg = 0;

        // Height validation
        if (heightStr.isEmpty()) {
            tilHeight.setError(getString(R.string.bmi_error_height_required));
            isValid = false;
        } else {
            try {
                heightCm = Double.parseDouble(heightStr);
                if (heightCm <= 0) {
                    tilHeight.setError(getString(R.string.bmi_error_height_positive));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilHeight.setError(getString(R.string.bmi_error_height_invalid));
                isValid = false;
            }
        }

        // Weight validation
        if (weightStr.isEmpty()) {
            tilWeight.setError(getString(R.string.bmi_error_weight_required));
            isValid = false;
        } else {
            try {
                weightKg = Double.parseDouble(weightStr);
                if (weightKg <= 0) {
                    tilWeight.setError(getString(R.string.bmi_error_weight_positive));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilWeight.setError(getString(R.string.bmi_error_weight_invalid));
                isValid = false;
            }
        }

        if (!isValid) {
            cardResult.setVisibility(View.GONE);
            return;
        }

        // Original Flask formula, preserved exactly:
        // height_m = height_cm / 100
        // bmi = weight_kg / (height_m ** 2)
        double heightMeters = heightCm / 100.0;
        double bmi = weightKg / (heightMeters * heightMeters);

        String formatted = String.format(Locale.US, "%.2f", bmi);
        tvBmiResult.setText(formatted);
        cardResult.setVisibility(View.VISIBLE);
    }
}