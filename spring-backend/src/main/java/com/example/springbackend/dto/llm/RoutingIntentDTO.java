package com.example.springbackend.dto.llm;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record RoutingIntentDTO(
        String startCoordinates,
        String endCoordinates,
        String startLocation,
        String endLocation,
        @JsonFormat(pattern = "HH:mm")
        LocalTime arrivalTime
) {}
