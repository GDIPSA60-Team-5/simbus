package com.example.springbackend.dto.llm;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record RoutingIntentDTO(
        String start,
        String end,
        @JsonFormat(pattern = "HH:mm")
        LocalTime arrivalTime
) {}
