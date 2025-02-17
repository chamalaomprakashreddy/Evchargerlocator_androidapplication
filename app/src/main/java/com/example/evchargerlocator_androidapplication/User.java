package com.example.evchargerlocator_androidapplication;

public class User {
    private String email;
    private String fullName;
    private String phoneNumber;
    private String vehicle;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String email, String fullName, String phoneNumber, String vehicle) {
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.vehicle = vehicle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
}
