package com.iot.aircraftNav;


public class Location {
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private double latitude;

    private double longitude;

    public Location(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
