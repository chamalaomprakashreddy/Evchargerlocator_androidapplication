package com.example.evchargerlocator_androidapplication;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String vehicle;
    private String lastSeen;  // ✅ Stores Online Status or Last Seen Time

    // ✅ Default Constructor (Required by Firebase)
    public User() {
    }

    // ✅ Constructor with Last Seen Status
    public User(String id, String email, String fullName, String phoneNumber, String vehicle, String lastSeen) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.vehicle = vehicle;
        this.lastSeen = lastSeen != null ? lastSeen : "Unknown"; // ✅ Ensures null safety
    }

    // ✅ Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen != null ? lastSeen : "Unknown"; // ✅ Prevents null issues
    }

    // ✅ Returns true if user is online
    public boolean isOnline() {
        return "Online".equals(lastSeen); // ✅ Improved null safety
    }
}
