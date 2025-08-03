package com.example.springbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LtaDtos {
    // For https://datamall2.mytransport.sg/ltaodataservice/BusStops
    public record BusStop(@JsonProperty("BusStopCode") String code,
                          @JsonProperty("Description") String description,
                          @JsonProperty("Latitude") double latitude,
                          @JsonProperty("Longitude") double longitude) {}
    public record LtaBusStopsResponse(@JsonProperty("value") List<BusStop> value) {}

    // For https://datamall2.mytransport.sg/ltaodataservice/v3/BusArrival
    public record NextBus(@JsonProperty("EstimatedArrival") String estimatedArrival) {}
    public record Service(@JsonProperty("ServiceNo") String serviceNo,
                          @JsonProperty("Operator") String operator,
                          @JsonProperty("NextBus") NextBus nextBus,
                          @JsonProperty("NextBus2") NextBus nextBus2,
                          @JsonProperty("NextBus3") NextBus nextBus3) {} // NextBus3 might be null
    public record LtaArrivalsResponse(@JsonProperty("Services") List<Service> services) {}
}
