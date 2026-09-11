package com.fitzone.app.network;

// ===============================
// ApiConfig.java
// ===============================
// WHAT THIS FILE DOES:
// Holds the ONE base URL Retrofit uses to reach your Flask backend.
// Nothing else in the app should hard-code a URL — everything points here.
//
// WHY:
// When you move from testing on an emulator to a real phone, or later to a
// real production server, you only ever change ONE line, in ONE file.

public class ApiConfig {

    // ---- Development (Android Emulator talking to Flask on YOUR computer) ----
    // 10.0.2.2 is a special address the emulator uses to mean
    // "the computer I'm running on" — "localhost" would NOT work here,
    // because inside the emulator, localhost means the emulator itself.
    // This value never needs to change, unlike your LAN IP below.
    public static final String BASE_URL_EMULATOR = "http://10.0.2.2:5000/";

    // ---- Development (real physical phone, same Wi-Fi as your computer) ----
    // Your computer's actual LAN IP. This CAN change (new Wi-Fi, router
    // restart) — if a physical-device test ever times out the same way the
    // emulator did, re-check this value with `ipconfig`.
    public static final String BASE_URL_LOCAL_NETWORK = "http://192.168.1.101:5000/";

    // ---- Production (once you deploy Flask somewhere real) ----
    // Leave this as a placeholder until you actually deploy. Do NOT put
    // real production secrets or credentials here — this is just a URL.
    public static final String BASE_URL_PRODUCTION = "https://your-production-domain.com/";

    // ===========================================================
    // ACTIVE URL — this is the one line the rest of the app uses.
    // Switch this constant while developing; nothing else needs to change.
    // ===========================================================
    public static final String BASE_URL = BASE_URL_EMULATOR;
}