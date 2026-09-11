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
import com.fitzone.app.models.MembershipResponse;
import com.fitzone.app.models.Plan;
import com.fitzone.app.models.PlansResponse;
import com.fitzone.app.models.UpdateMembershipRequest;
import com.fitzone.app.network.ApiService;
import com.fitzone.app.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        ApiService apiService = RetrofitClient.getApiService(requireContext().getApplicationContext());
        apiService.getPlans().enqueue(new Callback<PlansResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlansResponse> call,
                                   @NonNull Response<PlansResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() && response.body().getData() != null) {
                    List<Plan> plans = response.body().getData().getPlans();
                    displayPlans(plans);
                    loadMembership(); // secondary call — highlights the current plan
                } else {
                    showError(getString(R.string.error_load_plans));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlansResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showError("Network error. Please check your connection.");
            }
        });
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
        ApiService apiService = RetrofitClient.getApiService(requireContext().getApplicationContext());
        apiService.getMembership().enqueue(new Callback<MembershipResponse>() {
            @Override
            public void onResponse(@NonNull Call<MembershipResponse> call,
                                   @NonNull Response<MembershipResponse> response) {
                if (!isAdded()) return;

                if (response.code() == 401) {
                    // Token invalid/expired — same session-guard behavior as HomeFragment.
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                    return;
                }

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() && response.body().getData() != null) {
                    displayMembership(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MembershipResponse> call, @NonNull Throwable t) {
                // Fail silently — plans are already shown, this is a nice-to-have.
            }
        });
    }

    private void displayMembership(MembershipResponse.MembershipData data) {
        String plan = (data.getPlan() != null && !data.getPlan().isEmpty())
                ? data.getPlan() : "Not set";
        tvMyPlanName.setText(plan);

        if (planAdapter != null) {
            planAdapter.setCurrentPlanDuration(data.getPlan());
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

        ApiService apiService = RetrofitClient.getApiService(requireContext().getApplicationContext());
        UpdateMembershipRequest request = new UpdateMembershipRequest(plan.getDuration());

        apiService.updateMembership(request).enqueue(new Callback<MembershipResponse>() {
            @Override
            public void onResponse(@NonNull Call<MembershipResponse> call,
                                   @NonNull Response<MembershipResponse> response) {
                if (!isAdded()) return;
                rvPlans.setEnabled(true);

                if (response.code() == 401) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Membership updated successfully",
                            Toast.LENGTH_SHORT).show();
                    // Refresh so the new current-plan highlight and "My Membership"
                    // card reflect the change immediately — no app restart needed.
                    loadMembership();
                } else {
                    String message = (response.body() != null) ? response.body().getMessage()
                            : "Unable to update membership.";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MembershipResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                rvPlans.setEnabled(true);
                Toast.makeText(requireContext(), "Network error. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
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