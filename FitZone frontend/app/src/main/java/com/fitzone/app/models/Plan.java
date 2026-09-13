package com.fitzone.app.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single membership plan tier, as returned by GET /api/plans.
 *
 * IMPORTANT: "duration" is the field that matches members.plan in the
 * database (e.g. "1 Month", "3 Months", "6 Months", "12 Months").
 * "id" is just a stable key for Android's own use (e.g. list adapter),
 * it is NOT what's stored against a member — do not match on "id".
 */
public class Plan {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private int price;

    @SerializedName("duration")
    private String duration;

    @SerializedName("best_value")
    private boolean bestValue;

    public Plan(String id, String name, int price, String duration, boolean bestValue) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.bestValue = bestValue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDuration() {
        return duration;
    }

    public boolean isBestValue() {
        return bestValue;
    }
}