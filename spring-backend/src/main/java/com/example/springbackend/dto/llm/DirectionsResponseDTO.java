package com.example.springbackend.dto.llm;

import com.example.springbackend.model.Coordinates;

import java.util.List;

public record DirectionsResponseDTO(
        String startLocation,
        String endLocation,
        Coordinates startCoordinates,
        Coordinates endCoordinates,
        List<RouteDTO>  suggestedRoutes
) implements BotResponseDTO {

    @Override
    public String getType() {
        return "directions";
    }

    public record RouteDTO(
            int durationInMinutes,
            List<LegDTO> legs,
            String summary
    ) {}
    public record LegDTO(
            String type,
            int durationInMinutes,
            String busServiceNumber,
            String instruction,
            String legGeometry
    ) {}
}
