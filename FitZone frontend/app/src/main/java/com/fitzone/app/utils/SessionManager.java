package com.fitzone.app.utils;

// ===============================
// SessionManager.java
// ===============================
// WHAT THIS FILE DOES:
// The original Flask app kept the logged-in user in session["user"], stored
// automatically by the browser via a cookie. Android has no cookie jar, so
// once Phase 7 (Authentication) wires up login, it will store the token
// (from Phase 3's generate_token()) here instead, using SharedPreferences —
// Android's simple key-value local storage.
//
// NOT USED YET. Phase 5 only prepares this class. Phase 7 will call:
//     sessionManager.saveToken(token);
//     sessionManager.getToken();
//     sessionManager.isLoggedIn();
//     sessionManager.clearSession();   // on logout

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "FitZonePrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_MEMBER_ID = "member_id";
    private static final String KEY_FULLNAME = "fullname";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Call this right after a successful login (Phase 7), using the token
    // and member id returned by POST /api/auth/login.
    public void saveSession(String token, int memberId, String fullname) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_MEMBER_ID, memberId);
        editor.putString(KEY_FULLNAME, fullname);
        editor.apply();
    }

    // Returns the saved token, or null if nobody is logged in.
    // Later, this gets sent as: Authorization: Bearer <token>
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public int getMemberId() {
        return prefs.getInt(KEY_MEMBER_ID, -1);
    }

    public String getFullname() {
        return prefs.getString(KEY_FULLNAME, null);
    }

    // Used by LaunchActivity (Phase 2's navigation skeleton) to decide
    // whether to open Onboarding/Login or go straight to MainActivity.
    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // Call this on Logout — clears everything, matching the original app's
    // session.pop("user", None) behavior.
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}