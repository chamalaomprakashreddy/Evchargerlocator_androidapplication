package com.example.evchargerlocator_androidapplication;

public class Trip {
    private String tripId, name, date, startPoint, endPoint;
    private String level, connector, network;

    public Trip() {
    }

    public Trip(String tripId, String name, String date, String startPoint, String endPoint, String level, String connector, String network) {
        this.tripId = tripId;
        this.name = name;
        this.date = date;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.level = level;
        this.connector = connector;
        this.network = network;
    }

    public String getTripId() {
        return tripId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getLevel() {
        return level;
    }

    public String getConnector() {
        return connector;
    }

    public String getNetwork() {
        return network;
    }
}
