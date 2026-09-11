package com.fitzone.app.models;

// ===============================
// RegisterResponse.java
// ===============================
// Maps to api_register()'s two possible responses:
//
// Success (201): {"success": true, "message": "Registration successful", "data": {}}
// Error:         {"success": false, "message": "...", "error": "MISSING_FIELDS" /
//                 "PASSWORD_MISMATCH" / "DUPLICATE_EMAIL" / "DB_UNAVAILABLE" / "SERVER_ERROR"}
//
// No "data" field is needed here — success_response() is called with no
// data argument, so it defaults to {} and there's nothing useful to parse
// out of it (unlike login, which returns a token).

public class RegisterResponse {
    private boolean success;
    private String message;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}