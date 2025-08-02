package com.example.springbackend.dto;

public record ChatRequest(String input, Coordinates currentLocation, Long currentTimestamp) {
}