package com.example.springbackend.dto;

public record ChatRequest(String userInput, Coordinates currentLocation, Long currentTimestamp) {
}