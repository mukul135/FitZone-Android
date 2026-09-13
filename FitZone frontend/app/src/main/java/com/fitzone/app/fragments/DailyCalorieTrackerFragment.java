package com.fitzone.app.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitzone.app.BuildConfig;
import com.fitzone.app.R;
import com.fitzone.app.adapters.FoodEntryAdapter;
import com.fitzone.app.api.GeminiApi;
import com.fitzone.app.api.GeminiRequest;
import com.fitzone.app.api.GeminiResponse;
import com.fitzone.app.models.FoodEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DailyCalorieTrackerFragment extends Fragment {

    private TextInputLayout tilFoodName;
    private TextInputLayout tilQuantity;
    private TextInputEditText etFoodName;
    private TextInputEditText etQuantity;
    private TextView tvTotalCalories;
    private RecyclerView rvFoodEntries;
    private ProgressBar progressBar;
    private MaterialButton btnAddFood;

    private FoodEntryAdapter adapter;
    private List<FoodEntry> foodEntries = new ArrayList<>();
    private int totalCalories = 0;

    private GeminiApi geminiApi;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String todayDateString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_calorie_tracker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilFoodName = view.findViewById(R.id.tilFoodName);
        tilQuantity = view.findViewById(R.id.tilQuantity);
        etFoodName = view.findViewById(R.id.etFoodName);
        etQuantity = view.findViewById(R.id.etQuantity);
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        rvFoodEntries = view.findViewById(R.id.rvFoodEntries);
        progressBar = view.findViewById(R.id.progressBar);
        btnAddFood = view.findViewById(R.id.btnAddFood);

        adapter = new FoodEntryAdapter(foodEntries);
        rvFoodEntries.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFoodEntries.setAdapter(adapter);

        view.findViewById(R.id.ivBack).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        
        View historyBtn = view.findViewById(R.id.ivHistory);
        if (historyBtn != null) {
            historyBtn.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new CalorieHistoryFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        btnAddFood.setOnClickListener(v -> estimateAndAddFood());

        // Initialize Gemini
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geminiApi = retrofit.create(GeminiApi.class);

        // Initialize Firestore and fetch today's data
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        todayDateString = sdf.format(new Date());

        loadTodayData();
    }

    private void loadTodayData() {
        if (user == null) return;
        setLoadingState(true);
        db.collection("users")
                .document(user.getUid())
                .collection("calorie_logs")
                .document(todayDateString)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    setLoadingState(false);
                    if (documentSnapshot.exists()) {
                        Long totalCalObj = documentSnapshot.getLong("totalCalories");
                        if (totalCalObj != null) {
                            totalCalories = totalCalObj.intValue();
                        }
                        
                        List<Map<String, Object>> entriesList = (List<Map<String, Object>>) documentSnapshot.get("entries");
                        if (entriesList != null) {
                            foodEntries.clear();
                            for (Map<String, Object> map : entriesList) {
                                String name = (String) map.get("name");
                                Long calObj = (Long) map.get("calories");
                                int calories = calObj != null ? calObj.intValue() : 0;
                                foodEntries.add(new FoodEntry(name != null ? name : "", calories));
                            }
                            adapter.notifyDataSetChanged();
                            if (!foodEntries.isEmpty()) {
                                rvFoodEntries.scrollToPosition(foodEntries.size() - 1);
                            }
                        }
                    }
                    updateTotalCaloriesText();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    setLoadingState(false);
                    Log.e("CalorieTracker", "Failed to load today's data", e);
                    Toast.makeText(requireContext(), "Failed to load saved calories", Toast.LENGTH_SHORT).show();
                    updateTotalCaloriesText();
                });
    }

    private void estimateAndAddFood() {
        tilFoodName.setError(null);
        tilQuantity.setError(null);

        String foodName = etFoodName.getText() != null ? etFoodName.getText().toString().trim() : "";
        String quantity = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";

        boolean isValid = true;

        if (TextUtils.isEmpty(foodName)) {
            tilFoodName.setError(getString(R.string.daily_calorie_error_food_name));
            isValid = false;
        }

        if (TextUtils.isEmpty(quantity)) {
            tilQuantity.setError(getString(R.string.daily_calorie_error_quantity_required));
            isValid = false;
        }

        if (!isValid) return;

        if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
            Toast.makeText(requireContext(), "Gemini API Key is missing in local.properties", Toast.LENGTH_LONG).show();
            return;
        }

        setLoadingState(true);

        String prompt = "Estimate the total calories for " + quantity + " of " + foodName + ". Respond ONLY with the integer number of calories, nothing else. If you are unsure, provide your best reasonable guess as a single integer.";

        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
        GeminiRequest request = new GeminiRequest(Collections.singletonList(content));

        geminiApi.generateContent(BuildConfig.GEMINI_API_KEY, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeminiResponse> call, @NonNull Response<GeminiResponse> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        GeminiResponse geminiResponse = response.body();
                        if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                            GeminiResponse.Candidate candidate = geminiResponse.getCandidates().get(0);
                            if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                                String resultText = candidate.getContent().getParts().get(0).getText();
                                if (resultText != null) {
                                    // Extract digits only
                                    String digits = resultText.replaceAll("\\D+", "");
                                    if (!digits.isEmpty()) {
                                        int calories = Integer.parseInt(digits);
                                        addFoodEntry(foodName, quantity, calories);
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                setLoadingState(false);
                Log.e("GeminiAPI", "Failed to parse response: " + (response.errorBody() != null ? response.errorBody().toString() : ""));
                Toast.makeText(requireContext(), getString(R.string.daily_calorie_error_ai_fetch), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<GeminiResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setLoadingState(false);
                Log.e("GeminiAPI", "Request failed", t);
                Toast.makeText(requireContext(), getString(R.string.daily_calorie_error_ai_fetch), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFoodEntry(String foodName, String quantity, int calories) {
        String entryName = quantity + " " + foodName;
        FoodEntry entry = new FoodEntry(entryName, calories);
        foodEntries.add(entry);
        adapter.notifyItemInserted(foodEntries.size() - 1);
        rvFoodEntries.scrollToPosition(foodEntries.size() - 1);

        totalCalories += calories;
        updateTotalCaloriesText();

        etFoodName.setText("");
        etQuantity.setText("");
        etFoodName.clearFocus();
        etQuantity.clearFocus();
        
        saveToFirestore();
    }

    private void saveToFirestore() {
        if (user == null) {
            setLoadingState(false);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalCalories", totalCalories);
        data.put("entries", foodEntries);

        db.collection("users")
                .document(user.getUid())
                .collection("calorie_logs")
                .document(todayDateString)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        setLoadingState(false);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        setLoadingState(false);
                        Toast.makeText(requireContext(), "Failed to save calories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnAddFood.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnAddFood.setEnabled(true);
        }
    }

    private void updateTotalCaloriesText() {
        tvTotalCalories.setText(String.format(Locale.getDefault(), "%d kcal", totalCalories));
    }
}
