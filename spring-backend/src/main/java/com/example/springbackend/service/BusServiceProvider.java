package com.example.springbackend.service;

import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import reactor.core.publisher.Flux;

public interface BusServiceProvider {
    /**
     * Identifies which API this provider supports.
     */
    String getApiName();

    /**
     * Fetches all bus stops from the provider and caches them.
     */
    Flux<BusStop> getAllBusStops();

    /**
     * Gets arrival timings for a given bus stop code.
     */
    Flux<BusArrival> getBusArrivals(String busStopCode);
}