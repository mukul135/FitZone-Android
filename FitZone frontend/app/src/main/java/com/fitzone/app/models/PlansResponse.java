package com.fitzone.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Top-level response for GET /api/plans.
 *
 * Matches utils/responses.py's success_response() shape:
 * {
 *   "success": true,
 *   "message": "Plans fetched successfully",
 *   "data": { "plans": [ ... ] }
 * }
 */
public class PlansResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PlansData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public PlansData getData() {
        return data;
    }

    /**
     * Inner "data" object — holds the actual list of plans under the
     * "plans" key, exactly as api_plans() builds it in Flask.
     */
    public static class PlansData {
        @SerializedName("plans")
        private List<Plan> plans;

        public List<Plan> getPlans() {
            return plans;
        }
    }
}