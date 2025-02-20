package com.example.evchargerlocator_androidapplication;

public class Trip {
    private String tripId, name, date, startPoint, endPoint;

    public Trip() {
    }

    public Trip(String tripId, String name, String date, String startPoint, String endPoint) {
        this.tripId = tripId;
        this.name = name;
        this.date = date;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
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

}
