package com.fitzone.app.models;

public class ProgramsResponse {
    private boolean success;
    private String message;
    private ProgramsData data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public ProgramsData getData() { return data; }
}