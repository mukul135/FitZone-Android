package com.fitzone.app.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for POST /api/membership/update.
 * "plan" must be one of the duration strings /api/plans returns
 * (e.g. "1 Month", "3 Months", "6 Months", "12 Months") — the backend
 * validates this against VALID_PLAN_DURATIONS and returns 400 INVALID_PLAN
 * otherwise.
 */
public class UpdateMembershipRequest {

    @SerializedName("plan")
    private String plan;

    public UpdateMembershipRequest(String plan) {
        this.plan = plan;
    }

    public String getPlan() {
        return plan;
    }
}