package com.fitzone.app.models;

import com.google.gson.annotations.SerializedName;

// ===============================
// RegisterRequest.java
// ===============================
// Maps to the JSON body used by BOTH:
//   POST /api/auth/register
//   POST /api/register/<program_name>
// (they share the exact same required_fields list and INSERT columns,
// except the program-specific route also writes program_interest, which
// comes from the URL path, not this body).
//
// Field names use @SerializedName wherever Flask uses snake_case
// (confirm_password, medical_info, emergency_name, emergency_number).

public class RegisterRequest {

    private String fullname;
    private String email;
    private String mobile;
    private String password;

    @SerializedName("confirm_password")
    private String confirmPassword;

    private String dob;
    private String gender;
    private String height;
    private String weight;
    private String goal;
    private String plan;

    @SerializedName("medical_info")
    private String medicalInfo;

    @SerializedName("emergency_name")
    private String emergencyName;

    @SerializedName("emergency_number")
    private String emergencyNumber;

    public RegisterRequest(String fullname, String email, String mobile, String password,
                           String confirmPassword, String dob, String gender, String height,
                           String weight, String goal, String plan, String medicalInfo,
                           String emergencyName, String emergencyNumber) {
        this.fullname = fullname;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.dob = dob;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.goal = goal;
        this.plan = plan;
        this.medicalInfo = medicalInfo;
        this.emergencyName = emergencyName;
        this.emergencyNumber = emergencyNumber;
    }
}