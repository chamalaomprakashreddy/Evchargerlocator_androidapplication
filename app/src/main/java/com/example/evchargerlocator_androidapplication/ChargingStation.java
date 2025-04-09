package com.example.evchargerlocator_androidapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class ChargingStation implements Parcelable {
    private String stationId;
    private String name;
    private double latitude;
    private double longitude;
    private String powerOutput;
    private String availability;
    private String chargingLevel;
    private String connectorType;
    private String network;
    private String adminId;
    private int pricing;
    private int availablePorts;
    private int totalPorts;

    // 🔹 Full constructor
    public ChargingStation(String stationId, String name, double latitude, double longitude,
                           String powerOutput, String availability,
                           String chargingLevel, String connectorType,
                           String network, String adminId, int pricing) {
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
        this.pricing = pricing;
    }

    // 🔹 Minimal constructor
    public ChargingStation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ChargingStation() {}

    // 🔹 Getters
    public String getStationId() { return stationId; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getPowerOutput() { return powerOutput; }
    public String getAvailability() { return availability; }
    public String getChargingLevel() { return chargingLevel; }
    public String getConnectorType() { return connectorType; }
    public String getNetwork() { return network; }
    public String getAdminId() { return adminId; }
    public int getPricing() { return pricing; }
    public int getAvailablePorts() { return availablePorts; }
    public int getTotalPorts() { return totalPorts; }

    // 🔹 Setters
    public void setStationId(String stationId) { this.stationId = stationId; }
    public void setName(String name) { this.name = name; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setPowerOutput(String powerOutput) { this.powerOutput = powerOutput; }
    public void setAvailability(String availability) { this.availability = availability; }
    public void setChargingLevel(String chargingLevel) { this.chargingLevel = chargingLevel; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
    public void setNetwork(String network) { this.network = network; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public void setPricing(int pricing) { this.pricing = pricing; }
    public void setAvailablePorts(int availablePorts) { this.availablePorts = availablePorts; }
    public void setTotalPorts(int totalPorts) { this.totalPorts = totalPorts; }

    // 🔹 Parcelable implementation
    protected ChargingStation(Parcel in) {
        stationId = in.readString();
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        powerOutput = in.readString();
        availability = in.readString();
        chargingLevel = in.readString();
        connectorType = in.readString();
        network = in.readString();
        adminId = in.readString();
        availablePorts = in.readInt();
        totalPorts = in.readInt();
        pricing = in.readInt(); // ✅ correctly restored
    }

    public static final Creator<ChargingStation> CREATOR = new Creator<>() {
        @Override
        public ChargingStation createFromParcel(Parcel in) {
            return new ChargingStation(in);
        }

        @Override
        public ChargingStation[] newArray(int size) {
            return new ChargingStation[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(stationId);
        parcel.writeString(name);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(powerOutput);
        parcel.writeString(availability);
        parcel.writeString(chargingLevel);
        parcel.writeString(connectorType);
        parcel.writeString(network);
        parcel.writeString(adminId);
        parcel.writeInt(availablePorts);
        parcel.writeInt(totalPorts);
        parcel.writeInt(pricing); // ✅ must be last to match read order
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
