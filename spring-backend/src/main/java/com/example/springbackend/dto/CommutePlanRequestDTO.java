package com.example.springbackend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

public record CommutePlanRequestDTO(
        String id,
        @NotNull String commutePlanName,
        @NotNull LocalTime notifyAt,
        @NotNull LocalTime arrivalTime,
        Integer notificationNum,
        Boolean recurrence,
        String busStopCode,
        String busServiceNo,
        String startLocationId,
        String endLocationId,
        List<Boolean> selectedDays
) {}
