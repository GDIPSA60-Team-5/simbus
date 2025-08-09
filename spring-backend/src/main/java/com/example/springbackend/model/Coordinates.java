package com.example.springbackend.model;

public record Coordinates(double latitude, double longitude) {
    @Override
    public String toString() {
        return latitude + "," + longitude;
    }

    public static Coordinates fromString(String latLon) {
        try {
            String[] parts = latLon.split(",");
            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());
            return new Coordinates(lat, lon);
        } catch (Exception e) {
            return new Coordinates(0.0, 0.0);
        }
    }
}
