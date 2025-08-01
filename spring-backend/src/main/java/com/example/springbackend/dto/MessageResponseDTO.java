package com.example.springbackend.dto;

public class MessageResponseDTO extends BotResponseDTO {
    public final String type = "message"; // Field to match the client's expectation
    private String text;

    public MessageResponseDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getType() {
        return type;
    }

}