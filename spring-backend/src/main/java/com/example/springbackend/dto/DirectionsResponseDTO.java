package com.example.springbackend.dto;

import java.util.List;

/**
 * Data Transfer Object for the simplified bus route response.
 * This is what will be sent to the Android client.
 */
public record DirectionsResponseDTO(
        String startLocation,
        String endLocation,
        List<RouteDTO> suggestedRoutes
) implements BotResponseDTO {

    @Override
    public String getType() {
        // The type is now a method, not a field.
        return "directions";
    }
}