package com.example.springbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * A unified representation of bus arrival timings for a specific service at a stop.
 *
 * @param serviceName The bus service number (e.g., "12", "D1").
 * @param operator The bus operator (e.g., "GAS", "NUS").
 * @param arrivals A sorted list of estimated arrival times.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BusArrival(
        String serviceName,
        String operator,
        List<ZonedDateTime> arrivals
) {}