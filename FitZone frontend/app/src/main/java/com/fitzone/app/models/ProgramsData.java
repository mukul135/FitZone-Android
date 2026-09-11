package com.fitzone.app.models;

import java.util.List;

// Maps to the "data" object in GET /api/programs: { "programs": [...] }
public class ProgramsData {
    private List<Program> programs;

    public List<Program> getPrograms() { return programs; }
}