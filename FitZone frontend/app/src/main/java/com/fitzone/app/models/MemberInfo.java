package com.fitzone.app.models;

// Maps to the "member" object returned inside api_login()'s success_response:
//   "member": { "id": ..., "fullname": ..., "email": ... }
public class MemberInfo {
    private int id;
    private String fullname;
    private String email;

    public int getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }
}