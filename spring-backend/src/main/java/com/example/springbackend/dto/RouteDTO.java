package com.example.springbackend.dto;

import java.util.List;

public class RouteDTO {
    private String summary;
    private int durationInMinutes;
    private String routeGeometry; // This will hold the geometry string for drawing on a map
    private List<LegDTO> legs;

    // Getters and Setters
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public String getRouteGeometry() {
        return routeGeometry;
    }

    public void setRouteGeometry(String routeGeometry) {
        this.routeGeometry = routeGeometry;
    }

    public List<LegDTO> getLegs() {
        return legs;
    }

    public void setLegs(List<LegDTO> legs) {
        this.legs = legs;
    }
}