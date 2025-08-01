package com.example.springbackend.dto;

public class LegDTO {
    private String type; // e.g., "WALK", "BUS"
    private String instruction;
    private String busServiceNumber; // Nullable for walk legs
    private int durationInMinutes;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getBusServiceNumber() {
        return busServiceNumber;
    }

    public void setBusServiceNumber(String busServiceNumber) {
        this.busServiceNumber = busServiceNumber;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }
}