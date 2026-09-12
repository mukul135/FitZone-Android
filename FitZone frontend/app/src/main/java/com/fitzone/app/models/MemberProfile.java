package com.fitzone.app.models;

// Maps to the "data" object returned by GET /api/profile.
// Distinct from MemberInfo (login-only: id/fullname/email) — this has
// all 13 fields the profile endpoint actually returns.
public class MemberProfile {
    private int id;
    private String fullname;
    private String email;
    private String mobile;
    private String dob;
    private String gender;
    private double height;
    private double weight;
    private String goal;
    private String plan;
    private String medical_info;
    private String emergency_name;
    private String emergency_number;
    private String profile_image;

    public int getId() { return id; }
    public String getFullname() { return fullname; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public String getGoal() { return goal; }
    public String getPlan() { return plan; }
    public String getMedicalInfo() { return medical_info; }
    public String getEmergencyName() { return emergency_name; }
    public String getEmergencyNumber() { return emergency_number; }
    public String getProfileImage() { return profile_image; }
}