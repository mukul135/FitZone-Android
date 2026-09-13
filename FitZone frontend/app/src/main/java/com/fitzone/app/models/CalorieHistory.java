package com.fitzone.app.models;

public class CalorieHistory {
    private String date;
    private int totalCalories;

    public CalorieHistory() {}

    public CalorieHistory(String date, int totalCalories) {
        this.date = date;
        this.totalCalories = totalCalories;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }
}
