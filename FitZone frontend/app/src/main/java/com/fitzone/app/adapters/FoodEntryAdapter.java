package com.fitzone.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitzone.app.R;
import com.fitzone.app.models.FoodEntry;

import java.util.List;
import java.util.Locale;

public class FoodEntryAdapter extends RecyclerView.Adapter<FoodEntryAdapter.FoodEntryViewHolder> {

    private final List<FoodEntry> foodEntries;

    public FoodEntryAdapter(List<FoodEntry> foodEntries) {
        this.foodEntries = foodEntries;
    }

    @NonNull
    @Override
    public FoodEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_entry, parent, false);
        return new FoodEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodEntryViewHolder holder, int position) {
        FoodEntry entry = foodEntries.get(position);
        holder.tvFoodName.setText(entry.getName());
        holder.tvFoodCalories.setText(String.format(Locale.US, "%d kcal", entry.getCalories()));
    }

    @Override
    public int getItemCount() {
        return foodEntries.size();
    }

    static class FoodEntryViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName;
        TextView tvFoodCalories;

        public FoodEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodCalories = itemView.findViewById(R.id.tvFoodCalories);
        }
    }
}
