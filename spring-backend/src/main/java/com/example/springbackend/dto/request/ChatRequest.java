package com.example.springbackend.dto.request;

import com.example.springbackend.model.Coordinates;

public record ChatRequest(String userInput, Coordinates currentLocation, Long currentTimestamp) {
}