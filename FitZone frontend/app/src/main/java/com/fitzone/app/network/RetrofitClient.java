package com.fitzone.app.network;

// ===============================
// RetrofitClient.java
// ===============================
// WHAT THIS FILE DOES:
// Builds ONE shared Retrofit object for the whole app to use, instead of
// creating a new one every time we need to make a network call. This is
// the standard "singleton" pattern for Retrofit.
//
// NEW IN PHASE 7:
// Every request now automatically gets an
//     Authorization: Bearer <token>
// header attached, IF a token is currently saved in SessionManager (i.e.
// the user is logged in). This means Login/Register (which don't need a
// token) work exactly as before, and any future authenticated endpoint
// (GET /api/profile, etc. in Phase 8+) just works without extra code.
//
// WHY getApiService() NOW NEEDS A Context:
// SessionManager needs a Context to read SharedPreferences. Per the
// Phase 5 report, nothing calls getApiService() yet, so changing its
// signature now is safe — no existing call sites break.
//
// HOW IT'S USED (Phase 7 onward):
//     ApiService api = RetrofitClient.getApiService(getApplicationContext());
//     api.login(requestBody).enqueue(new Callback<...>() { ... });

import android.content.Context;

import com.fitzone.app.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // Holds the single Retrofit instance once it's built.
    private static Retrofit retrofit = null;

    // Holds the single ApiService instance once it's built.
    private static ApiService apiService = null;

    // Private constructor — nobody should create an instance of this class
    // with "new RetrofitClient()". Everything is accessed as static methods.
    private RetrofitClient() {
    }

    private static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            final SessionManager sessionManager =
                    new SessionManager(context.getApplicationContext());

            // Runs on every outgoing request. Attaches the saved token if
            // one exists; leaves the request untouched if not (e.g. before
            // login, or for endpoints that don't require a token).
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    String token = sessionManager.getToken();

                    if (token == null) {
                        return chain.proceed(originalRequest);
                    }

                    Request authorizedRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();

                    return chain.proceed(authorizedRequest);
                }
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // This is the method the rest of the app will call to get access to
    // the API endpoints defined in ApiService.java.
    // NOTE: now requires a Context — pass getApplicationContext().
    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            apiService = getRetrofitInstance(context).create(ApiService.class);
        }
        return apiService;
    }
}