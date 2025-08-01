package com.example.springbackend.dto;

import java.util.List;

/**
 * Data Transfer Object for the simplified bus route response.
 * This is what will be sent to the Android client.
 */
public class DirectionsResponseDTO extends BotResponseDTO {
    public final String type = "directions";
    private String startLocation;
    private String endLocation;
    private List<RouteDTO> suggestedRoutes;

    // Getters and Setters
    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public List<RouteDTO> getSuggestedRoutes() {
        return suggestedRoutes;
    }

    public void setSuggestedRoutes(List<RouteDTO> suggestedRoutes) {
        this.suggestedRoutes = suggestedRoutes;
    }

    @Override
    public String getType() {
        return type;
    }
}