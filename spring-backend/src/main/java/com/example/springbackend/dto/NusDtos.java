package com.example.springbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class NusDtos {

    // For https://nnextbus.nus.edu.sg/BusStops
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BusStop(String caption, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BusStopsResult(List<BusStop> busstops) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NusBusStopsResponse(BusStopsResult BusStopsResult) {}

    // For https://nnextbus.nus.edu.sg/ShuttleService
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Eta(int eta) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Shuttle(String name, List<Eta> _etas) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ShuttleServiceResult(List<Shuttle> shuttles) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NusArrivalsResponse(ShuttleServiceResult ShuttleServiceResult) {}
}
