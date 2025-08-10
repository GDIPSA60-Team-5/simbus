// src/main/java/com/yourproject/service/LtaBusServiceProvider.java
package com.example.springbackend.service.implementation;

import com.example.springbackend.dto.LtaDtos;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.model.BusArrival;
import com.example.springbackend.service.BusServiceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

// ... other imports for models, DTOs, ZonedDateTime, etc.

@Service
public class LtaBusServiceProvider implements BusServiceProvider {

    private static final String API_NAME = "LTA";
    private final WebClient webClient;

    @Value("${api.lta.key}")
    private String apiKey;

    public LtaBusServiceProvider(@Qualifier("ltaWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getApiName() {
        return API_NAME;
    }

    //TODO: support pagination for all bus stops (LTA API supports pagination)
    @Override
    @Cacheable("ltaBusStops")
    public Flux<BusStop> getAllBusStops() {
        // LTA API paginates every 500 results. A real implementation needs to loop.
        // For simplicity, we'll just fetch the first page.
        return webClient.get()
                .uri("/BusStops")
                .header("AccountKey", apiKey)
                .retrieve()
                .bodyToMono(LtaDtos.LtaBusStopsResponse.class)
                .flatMapMany(response -> Flux.fromIterable(response.value()))
                .map(ltaStop -> new BusStop(
                        ltaStop.code(),
                        ltaStop.description(),
                        ltaStop.latitude(),
                        ltaStop.longitude(),
                        API_NAME
                ));
    }
    
    private Mono<List<LtaDtos.BusStop>> fetchBusStopsPage(int skip) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/BusStops")
                        .queryParam("$skip", skip)
                        .build())
                .header("AccountKey", apiKey)
                .retrieve()
                .bodyToMono(LtaDtos.LtaBusStopsResponse.class)
                .map(LtaDtos.LtaBusStopsResponse::value);
    }

    @Override
    public Flux<BusArrival> getBusArrivals(String busStopCode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v3/BusArrival") // Using v2 as per your sample
                        .queryParam("BusStopCode", busStopCode)
                        .build())
                .header("AccountKey", apiKey)
                .retrieve()
                .bodyToMono(LtaDtos.LtaArrivalsResponse.class)
                .flatMapMany(response -> Flux.fromIterable(response.services()))
                .map(this::mapLtaServiceToBusArrival);
    }

    private BusArrival mapLtaServiceToBusArrival(LtaDtos.Service service) {
        List<ZonedDateTime> arrivals = Stream.of(service.nextBus(), service.nextBus2(), service.nextBus3())
                .filter(Objects::nonNull)
                .map(LtaDtos.NextBus::estimatedArrival)
                .filter(timeStr -> timeStr != null && !timeStr.isEmpty())
                .map(ZonedDateTime::parse)
                .toList();

        return new BusArrival(service.serviceNo(), service.operator(), arrivals);
    }
}