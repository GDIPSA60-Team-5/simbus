package com.example.springbackend.dto.llm;

import com.example.springbackend.model.BusArrival;
import java.util.List;

/**
 * Represents the next bus arrival information at a stop.
 */
public record NextBusResponseDTO(
        String stopCode,          // bus stop code
        String stopName,          // bus stop name
        List<BusArrival> services // list of bus arrivals for each service
) implements BotResponseDTO {

    @Override
    public String getType() {
        return "next-bus";
    }
}