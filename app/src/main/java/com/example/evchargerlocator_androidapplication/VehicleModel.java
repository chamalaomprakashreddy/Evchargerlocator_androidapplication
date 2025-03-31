package com.example.evchargerlocator_androidapplication;

public class VehicleModel {
    public String name;
    public double batteryCapacityKWh;
    public double maxRangeKm;
    public double maxChargingSpeedKW;

    public VehicleModel(String name, double batteryCapacityKWh, double maxRangeKm, double maxChargingSpeedKW) {
        this.name = name;
        this.batteryCapacityKWh = batteryCapacityKWh;
        this.maxRangeKm = maxRangeKm;
        this.maxChargingSpeedKW = maxChargingSpeedKW;
    }
}
