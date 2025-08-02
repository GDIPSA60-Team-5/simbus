package com.example.springbackend.dto;

/**
 * Represents an error message response from the bot.
 * As a record, it's immutable and concise.
 * The JSON output will be {"type":"error", "message":"..."}
 */
public record ErrorResponseDTO(String message) implements BotResponseDTO {

    @Override
    public String getType() {
        return "error";
    }
}