package com.fitzone.app.models;

// Maps to the "data" object in api_login()'s success_response:
//   "data": { "token": "...", "member": { ... } }
public class LoginData {
    private String token;
    private MemberInfo member;

    public String getToken() {
        return token;
    }

    public MemberInfo getMember() {
        return member;
    }
}