package com.example.evchargerlocator_androidapplication;

import java.io.Serializable;

/**
 * Represents a segment in the user's EV trip.
 */
public class TripSegment implements Serializable {
    private String stationName;
    private String network;
    private String activationMethod;
    private double distance; // in miles
    private int availablePorts;
    private int totalPorts;

    public TripSegment(String stationName, String network, String activationMethod,
                       double distance, int availablePorts, int totalPorts) {
        this.stationName = stationName;
        this.network = network;
        this.activationMethod = activationMethod;
        this.distance = distance;
        this.availablePorts = availablePorts;
        this.totalPorts = totalPorts;
    }

    // Getters
    public String getStationName() {
        return stationName;
    }

    public String getNetwork() {
        return network;
    }

    public String getActivationMethod() {
        return activationMethod;
    }

    public double getDistance() {
        return distance;
    }

    public int getAvailablePorts() {
        return availablePorts;
    }

    public int getTotalPorts() {
        return totalPorts;
    }

    // Optionally add setters if needed in future
}
