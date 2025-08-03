package com.example.springbackend.model;

public record Coordinates(String latitude, String longitude) {
    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}
