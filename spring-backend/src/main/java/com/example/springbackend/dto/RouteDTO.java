package com.example.springbackend.dto;

import java.util.List;

public record RouteDTO(
        int durationInMinutes,
        List<LegDTO> legs,
        String summary,
        String routeGeometry
) {}