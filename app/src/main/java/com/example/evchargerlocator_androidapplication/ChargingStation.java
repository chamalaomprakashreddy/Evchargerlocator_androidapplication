package com.example.evchargerlocator_androidapplication;
public class ChargingStation {
    private String stationId;
    private String name;
    private double latitude;
    private double longitude;
    private String powerOutput;
    private String availability;
    private String chargingLevel;
    private String connectorType;
    private String network;

    public ChargingStation() {
        // Default constructor required for Firebase
    }

    public ChargingStation(String stationId, String name, double latitude, double longitude, String powerOutput,
                           String availability, String chargingLevel, String connectorType, String network) {
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.powerOutput = powerOutput;
        this.availability = availability;
        this.chargingLevel = chargingLevel;
        this.connectorType = connectorType;
        this.network = network;
    }

    public String getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPowerOutput() {
        return powerOutput;
    }

    public String getAvailability() {
        return availability;
    }

    public String getChargingLevel() {
        return chargingLevel;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public String getNetwork() {
        return network;
    }
}