package com.example.evchargerlocator_androidapplication;

public class User {
    private String id;
    private String fullName;
    private String status;

    public User() {}

    public User(String id, String fullName, String status) {
        this.id = id;
        this.fullName = fullName;
        this.status = status != null ? status : "Offline"; // Ensure it defaults to "Offline"
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOnline() {
        return "Online".equals(status);
    }
}
