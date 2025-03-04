package com.example.evchargerlocator_androidapplication;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String vehicle;

    // Default Constructor (Required by Firebase)
    public User() {
    }

    // ✅ Constructor without "status"
    public User(String id, String email, String fullName, String phoneNumber, String vehicle) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.vehicle = vehicle;
    }

    // ✅ Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getVehicle() { return vehicle; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }
}
