package com.fitzone.app.fragments;

import android.os.Bundle;
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

import com.fitzone.app.R;
import com.fitzone.app.activities.MainActivity;
import com.fitzone.app.adapters.PlanAdapter;
import com.fitzone.app.models.Plan;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MembershipFragment extends Fragment {

    private TextView tvMyPlanName, tvErrorMessage;
    private ProgressBar progressBarMembership;
    private View layoutError, scrollContent;
    private RecyclerView rvPlans;
    private MaterialButton btnRetry;

    private PlanAdapter planAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // FIX (Step 1 audit): was previously inflating R.layout.fragment_tools
        // by mistake — fragment_membership.xml was never actually shown.
        View view = inflater.inflate(R.layout.fragment_membership, container, false);

        initializeViews(view);
        btnRetry.setOnClickListener(v -> loadPlans());

        loadPlans();

        return view;
    }

    private void initializeViews(View view) {
        tvMyPlanName = view.findViewById(R.id.tvMyPlanName);
        progressBarMembership = view.findViewById(R.id.progressBarMembership);
        layoutError = view.findViewById(R.id.layoutError);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        btnRetry = view.findViewById(R.id.btnRetry);
        scrollContent = view.findViewById(R.id.scrollContent);

        rvPlans = view.findViewById(R.id.rvPlans);
        rvPlans.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPlans.setNestedScrollingEnabled(false);
        rvPlans.setHasFixedSize(false);
    }

    /**
     * Plans are the primary content of this screen (static, no-auth), so a
     * failure here shows the full error state — same reasoning HomeFragment
     * uses for getProfile() being primary and getPrograms() being secondary.
     */
    private void loadPlans() {
        showLoading();

        List<Plan> staticPlans = new ArrayList<>();
        staticPlans.add(new Plan("basic", "Basic", 999, "1 Month", false));
        staticPlans.add(new Plan("pro", "Pro", 2499, "3 Months", false));
        staticPlans.add(new Plan("elite", "Elite", 4499, "6 Months", true));
        staticPlans.add(new Plan("yearly", "Yearly", 7999, "12 Months", false));

        displayPlans(staticPlans);
        loadMembership(); // secondary call — highlights the current plan
    }

    private void displayPlans(List<Plan> plans) {
        showContent();

        planAdapter = new PlanAdapter(plans, this::confirmJoinPlan);
        rvPlans.setAdapter(planAdapter);

        // FIX: a wrap_content RecyclerView nested in a ScrollView
        // (nestedScrollingEnabled=false) can be measured by the ScrollView
        // before the adapter's real item count/height is known, clipping
        // the last item (confirmed via the 1000dp diagnostic test — Yearly
        // plan was missing at wrap_content, present with a fixed height).
        // Posting a requestLayout() after the adapter is attached forces
        // the parent to re-measure using the actual content height.
        rvPlans.post(rvPlans::requestLayout);
    }

    /**
     * Membership (the member's current plan) is secondary here — plans are
     * already shown successfully by this point. If this call fails, we fail
     * silently (no error banner) and simply leave no plan highlighted,
     * matching HomeFragment's treatment of getPrograms().
     */
    private void loadMembership() {
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
                        displayMembership(documentSnapshot);
                    }
                });
    }

    private void displayMembership(DocumentSnapshot documentSnapshot) {
        String plan = documentSnapshot.getString("plan");
        if (plan == null || plan.isEmpty()) {
            plan = "Not set";
        }
        tvMyPlanName.setText(plan);

        if (planAdapter != null) {
            planAdapter.setCurrentPlanDuration(plan);
            planAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Confirms before mutating the member's plan — prevents accidental taps
     * from silently changing a paid membership, and gives the user a chance
     * to back out. No payment step exists (Phase 9 Section 25) — this only
     * changes the stored plan string, matching what the confirmation text
     * says.
     */
    private void confirmJoinPlan(Plan plan) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Switch to " + plan.getName() + "?")
                .setMessage("Your membership plan will be updated to " + plan.getName()
                        + " (" + plan.getDuration() + ", ₹" + plan.getPrice() + ").")
                .setPositiveButton("Confirm", (dialog, which) -> updateMembership(plan))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateMembership(Plan plan) {
        // Prevent duplicate taps while the request is in flight.
        rvPlans.setEnabled(false);
        Toast.makeText(requireContext(), "Updating membership...", Toast.LENGTH_SHORT).show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            rvPlans.setEnabled(true);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
                .update("plan", plan.getDuration())
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    rvPlans.setEnabled(true);
                    Toast.makeText(requireContext(), "Membership updated successfully",
                            Toast.LENGTH_SHORT).show();
                    // Refresh so the new current-plan highlight and "My Membership"
                    // card reflect the change immediately — no app restart needed.
                    loadMembership();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    rvPlans.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to update membership.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading() {
        progressBarMembership.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBarMembership.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBarMembership.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }
}