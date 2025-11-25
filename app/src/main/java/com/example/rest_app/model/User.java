package com.example.rest_app.model;

public class User {
    private String userId;
    private String email;
    private long createdAt;
    private String name;

    // Required empty constructor for Firestore
    public User() {
    }

    public User(String userId, String email, long createdAt, String name) {
        this.userId = userId;
        this.email = email;
        this.createdAt = createdAt;
        this.name = name;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}