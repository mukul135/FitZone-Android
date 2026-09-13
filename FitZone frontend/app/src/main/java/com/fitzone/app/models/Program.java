package com.fitzone.app.models;

public class Program {
    private String id;
    private String name;
    private String description;
    
    public Program(String id, String name, String description, String ignored) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}