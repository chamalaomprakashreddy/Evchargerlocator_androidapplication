package com.example.evchargerlocator_androidapplication;
public class ChargingStation {

    public String stationId, name, powerOutput, availability, chargingLevel, connectorType, network, adminId;
    public double latitude, longitude;

    public ChargingStation() {
        // Firebase required empty constructor
    }

    public ChargingStation(String stationId, String name, double latitude, double longitude,
                           String powerOutput, String availability,
                           String chargingLevel, String connectorType, String network, String adminId) {
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.powerOutput = powerOutput;
        this.availability = availability;
        this.chargingLevel = chargingLevel;
        this.connectorType = connectorType;
        this.network = network;
        this.adminId = adminId;
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