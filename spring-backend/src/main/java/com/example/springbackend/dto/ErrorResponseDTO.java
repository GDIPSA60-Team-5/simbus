package com.example.springbackend.dto;

public class ErrorResponseDTO extends BotResponseDTO {
    public final String type = "error"; // Field to match the client's expectation
    private String message;

    public ErrorResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getType() {
        return type;
    }

}