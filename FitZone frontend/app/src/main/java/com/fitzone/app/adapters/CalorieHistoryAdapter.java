package com.fitzone.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitzone.app.R;
import com.fitzone.app.models.CalorieHistory;

import java.util.List;

public class CalorieHistoryAdapter extends RecyclerView.Adapter<CalorieHistoryAdapter.ViewHolder> {

    private final List<CalorieHistory> historyList;

    public CalorieHistoryAdapter(List<CalorieHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calorie_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalorieHistory item = historyList.get(position);
        holder.tvDate.setText(item.getDate());
        holder.tvTotalCalories.setText(item.getTotalCalories() + " kcal");
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvTotalCalories;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotalCalories = itemView.findViewById(R.id.tvTotalCalories);
        }
    }
}
