package com.fitzone.app.models;

import com.google.gson.annotations.SerializedName;

/**
 * Top-level response for GET /api/membership (auth required).
 *
 * Matches utils/responses.py's success_response() shape:
 * {
 *   "success": true,
 *   "message": "Membership fetched successfully",
 *   "data": { "member_id": ..., "fullname": ..., "plan": ... }
 * }
 *
 * NOTE: "plan" here is a raw free-text string (e.g. "6 Months"), matching
 * members.plan exactly. There is no expiry/status field in the database
 * (Phase 1 §8) — do not add one to this model without a real backend change.
 */
public class MembershipResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private MembershipData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public MembershipData getData() {
        return data;
    }

    /**
     * Inner "data" object for /api/membership.
     */
    public static class MembershipData {
        @SerializedName("member_id")
        private int memberId;

        @SerializedName("fullname")
        private String fullname;

        @SerializedName("plan")
        private String plan;

        public int getMemberId() {
            return memberId;
        }

        public String getFullname() {
            return fullname;
        }

        /**
         * Matches a Plan's getDuration() value exactly
         * (e.g. "1 Month", "3 Months", "6 Months", "12 Months").
         * Use this to highlight the member's current plan card
         * in the Membership screen.
         */
        public String getPlan() {
            return plan;
        }
    }
}