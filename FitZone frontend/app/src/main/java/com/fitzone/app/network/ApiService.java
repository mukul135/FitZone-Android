package com.fitzone.app.network;

// ===============================
// ApiService.java
// ===============================
// PHASE 7 STEP 5 CHANGE:
// - register() now uses typed RegisterRequest/RegisterResponse (same
//   reasoning as login() in Step 4 — nothing else called this method yet).
// - Added registerForProgram(), matching the real
//   POST /api/register/<program_name> route: the program name is a URL
//   path segment (@Path), while the other 14 fields are the same JSON
//   body as plain register().

import com.fitzone.app.models.LoginRequest;
import com.fitzone.app.models.LoginResponse;
import com.fitzone.app.models.RegisterRequest;
import com.fitzone.app.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.Map;
import com.fitzone.app.models.ProfileResponse;
import com.fitzone.app.models.ProgramsResponse;
import com.fitzone.app.models.MembershipResponse;
import com.fitzone.app.models.PlansResponse;
import com.fitzone.app.models.UpdateMembershipRequest;
import com.fitzone.app.models.ChangePasswordRequest;
import com.fitzone.app.models.BaseResponse;

public interface ApiService {

    // ---- Built in Phase 3 Step 2 — real, working endpoints ----

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    // Matches POST /api/register/<program_name>. programName must be one of
    // VALID_PROGRAM_IDS on the Flask side (e.g. "Weight-Training") — Flask
    // returns 400 INVALID_PROGRAM otherwise.
    @POST("api/register/{program_name}")
    Call<RegisterResponse> registerForProgram(
            @Path("program_name") String programName,
            @Body RegisterRequest registerRequest
    );

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // ---- NOT YET BUILT on the Android side (Flask routes exist per the
    // full app.py you shared, but these aren't wired into any screen yet —
    // that's Phase 8+). Left here as a checklist:
    //
    @GET("api/profile")
    Call<ProfileResponse> getProfile();

    @GET("api/membership")
    Call<MembershipResponse> getMembership();

    @GET("api/plans")
    Call<PlansResponse> getPlans();

    @GET("api/programs")
    Call<ProgramsResponse> getPrograms();
    @POST("api/membership/update")
    Call<MembershipResponse> updateMembership(@Body UpdateMembershipRequest request);

    @POST("api/auth/change-password")
    Call<BaseResponse> changePassword(@Body ChangePasswordRequest request);

    @POST("api/contact")
    Call<Map<String, Object>> submitContact(@Body Map<String, String> contactRequest);
}