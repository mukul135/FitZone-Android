package com.fitzone.app.models;

// ===============================
// LoginResponse.java
// ===============================
// Maps to EITHER shape api_login() can return:
//
// Success:
//   {"success": true,  "message": "Login successful",
//    "data": {"token": "...", "member": {...}}}
//
// Error (e.g. MISSING_FIELDS / INVALID_CREDENTIALS / DB_UNAVAILABLE / SERVER_ERROR):
//   {"success": false, "message": "...", "error": "INVALID_CREDENTIALS"}
//
// Gson leaves "data" as null when parsing an error response (the field
// isn't present in the JSON), and leaves "error" as null when parsing a
// success response — so one class safely covers both cases.

public class LoginResponse {
    private boolean success;
    private String message;
    private LoginData data;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public LoginData getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}