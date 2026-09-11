package com.fitzone.app.models;

// ===============================
// LoginRequest.java
// ===============================
// WHAT THIS FILE DOES:
// Represents the JSON body sent to POST /api/auth/login. Gson (via
// Retrofit's GsonConverterFactory, already wired in RetrofitClient)
// turns this object into:
//   { "email": "...", "password": "..." }
// automatically.

public class LoginRequest {

    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}