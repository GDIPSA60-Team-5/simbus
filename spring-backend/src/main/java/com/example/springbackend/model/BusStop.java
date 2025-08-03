package com.example.springbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A unified representation of a bus stop from any provider.
 *
 * @param code The unique identifier for the bus stop (e.g., "01012", "UTOWN").
 * @param name A user-friendly name or description (e.g., "Hotel Grand Pacific").
 * @param latitude The latitude coordinate.
 * @param longitude The longitude coordinate.
 * @param sourceApi An identifier for the source API (e.g., "LTA", "NUS").
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record BusStop(
        String code,
        String name,
        double latitude,
        double longitude,
        String sourceApi
) {}