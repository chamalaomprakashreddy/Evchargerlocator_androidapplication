package com.example.evchargerlocator_androidapplication;

import java.util.List;

public class SavedTrip {
    // Firebase-generated key used for deleting or updating this trip
    private String id;

    private String tripName;
    private String fromAddress;
    private String toAddress;
    private String fromLatLng;
    private String toLatLng;
    private List<ChargingStation> stations;

    // Required empty constructor for Firebase
    public SavedTrip() {
    }

    public SavedTrip(String id, String tripName, String fromAddress, String toAddress,
                     String fromLatLng, String toLatLng, List<ChargingStation> stations) {
        this.id = id;
        this.tripName = tripName;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.fromLatLng = fromLatLng;
        this.toLatLng = toLatLng;
        this.stations = stations;
    }

    // Constructor without id (used while saving new trip, id is generated later)
    public SavedTrip(String tripName, String fromAddress, String toAddress,
                     String fromLatLng, String toLatLng, List<ChargingStation> stations) {
        this.tripName = tripName;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.fromLatLng = fromLatLng;
        this.toLatLng = toLatLng;
        this.stations = stations;
    }

    // ✅ Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getFromLatLng() {
        return fromLatLng;
    }

    public void setFromLatLng(String fromLatLng) {
        this.fromLatLng = fromLatLng;
    }

    public String getToLatLng() {
        return toLatLng;
    }

    public void setToLatLng(String toLatLng) {
        this.toLatLng = toLatLng;
    }

    public List<ChargingStation> getStations() {
        return stations;
    }

    public void setStations(List<ChargingStation> stations) {
        this.stations = stations;
    }
}
