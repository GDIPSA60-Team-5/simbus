package com.example.springbackend.dto.llm;

public record MessageResponseDTO(String message) implements BotResponseDTO {
    @Override
    public String getType() {
        return "message";
    }
}