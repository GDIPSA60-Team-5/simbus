package com.example.springbackend.dto.llm;

import java.util.List;

public record DirectionsResponseDTO(
        String startLocation,
        String endLocation,
        List<RouteDTO> suggestedRoutes
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
