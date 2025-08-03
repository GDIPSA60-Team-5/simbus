package com.example.springbackend.dto;

public record MessageResponseDTO(String message) implements BotResponseDTO {
    @Override
    public String getType() {
        return "message";
    }
}