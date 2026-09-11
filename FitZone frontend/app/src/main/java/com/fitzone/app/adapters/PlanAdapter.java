package com.fitzone.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitzone.app.R;
import com.fitzone.app.models.Plan;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    public interface OnPlanJoinListener {
        void onJoinClick(Plan plan);
    }

    private final List<Plan> plans;
    private final OnPlanJoinListener listener;

    // The member's current plan, matched against Plan.getDuration() —
    // NOT Plan.getId(). members.plan stores duration strings
    // (e.g. "6 Months"), confirmed via SELECT DISTINCT plan FROM members.
    // Null/empty means no membership data available yet (e.g. still loading).
    private String currentPlanDuration;

    public PlanAdapter(List<Plan> plans, OnPlanJoinListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    /**
     * Call this once /api/membership has returned, then notifyDataSetChanged()
     * so the matching card highlights and its button switches to "Current Plan".
     */
    public void setCurrentPlanDuration(String currentPlanDuration) {
        this.currentPlanDuration = currentPlanDuration;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = plans.get(position);
        Context context = holder.itemView.getContext();

        holder.tvPlanName.setText(plan.getName());
        holder.tvPlanPrice.setText(context.getString(R.string.plan_price_format, plan.getPrice()));
        holder.tvPlanDuration.setText(plan.getDuration());

        holder.chipBestValue.setVisibility(plan.isBestValue() ? View.VISIBLE : View.GONE);

        boolean isCurrent = currentPlanDuration != null
                && currentPlanDuration.equalsIgnoreCase(plan.getDuration());

        holder.tvCurrentPlanLabel.setVisibility(isCurrent ? View.VISIBLE : View.GONE);

        if (isCurrent) {
            holder.cardPlan.setStrokeColor(context.getResources().getColor(R.color.colorPrimary));
            holder.cardPlan.setStrokeWidth(dpToPx(context, 2));
            holder.btnJoinPlan.setText(R.string.plan_current_button);
            holder.btnJoinPlan.setEnabled(false);
        } else {
            holder.cardPlan.setStrokeColor(context.getResources().getColor(R.color.colorBorder));
            holder.cardPlan.setStrokeWidth(dpToPx(context, 1));
            holder.btnJoinPlan.setText(R.string.plan_join_button);
            holder.btnJoinPlan.setEnabled(true);
            holder.btnJoinPlan.setOnClickListener(v -> listener.onJoinClick(plan));
        }
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardPlan;
        TextView tvPlanName, tvPlanPrice, tvPlanDuration, tvCurrentPlanLabel;
        Chip chipBestValue;
        MaterialButton btnJoinPlan;

        PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPlan = itemView.findViewById(R.id.cardPlan);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanPrice = itemView.findViewById(R.id.tvPlanPrice);
            tvPlanDuration = itemView.findViewById(R.id.tvPlanDuration);
            tvCurrentPlanLabel = itemView.findViewById(R.id.tvCurrentPlanLabel);
            chipBestValue = itemView.findViewById(R.id.chipBestValue);
            btnJoinPlan = itemView.findViewById(R.id.btnJoinPlan);
        }
    }
}