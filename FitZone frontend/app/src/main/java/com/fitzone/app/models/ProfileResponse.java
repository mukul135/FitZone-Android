package com.fitzone.app.models;

// Wraps GET /api/profile's full response:
// { "success": true, "message": "...", "data": { ...member fields... } }
public class ProfileResponse {
    private boolean success;
    private String message;
    private MemberProfile data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public MemberProfile getData() { return data; }
}