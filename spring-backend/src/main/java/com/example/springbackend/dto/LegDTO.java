package com.example.springbackend.dto;

public record LegDTO(
        String type,
        int durationInMinutes,
        String busServiceNumber,
        String instruction
) {}