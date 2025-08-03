package com.example.springbackend.service.implementation;


import com.example.springbackend.dto.NusDtos;
import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.service.BusServiceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class NusBusServiceProvider implements BusServiceProvider {
    private static final String API_NAME = "NUS";
    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(NusBusServiceProvider.class);

    @Value("${api.nus.auth}")
    private String authKey;

    public NusBusServiceProvider(@Qualifier("nusWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getApiName() {
        return API_NAME;
    }

    @Override
    @Cacheable("nusBusStops")
    public Flux<BusStop> getAllBusStops() {
        return webClient.get()
                .uri("/BusStops")
                .header("Authorization", authKey)
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> log.info("Raw /BusStops response body: {}", body))
                .flatMapMany(body -> {
                    try {
                        NusDtos.NusBusStopsResponse response = new ObjectMapper().readValue(body, NusDtos.NusBusStopsResponse.class);
                        return Flux.fromIterable(response.BusStopsResult().busstops());
                    } catch (Exception e) {
                        log.error("Failed to parse /BusStops response", e);
                        return Flux.empty();
                    }
                })
                .map(nusStop -> new BusStop(
                        nusStop.name(),
                        nusStop.caption(),
                        0.0, 0.0,
                        API_NAME
                ));
    }

    @Override
    public Flux<BusArrival> getBusArrivals(String busStopCode) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Singapore"));
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/ShuttleService")
                        .queryParam("busstopname", busStopCode)
                        .build())
                .header("Authorization", authKey)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> log.info("Raw /ShuttleService response body: {}", body))
                .flatMapMany(body -> {
                    try {
                        NusDtos.NusArrivalsResponse response = new ObjectMapper().readValue(body, NusDtos.NusArrivalsResponse.class);
                        return Flux.fromIterable(response.ShuttleServiceResult().shuttles());
                    } catch (Exception e) {
                        log.error("Failed to parse /ShuttleService response", e);
                        return Flux.empty();
                    }
                })
                .map(shuttle -> {
                    List<ZonedDateTime> arrivals = shuttle._etas().stream()
                            .map(eta -> now.plusMinutes(eta.eta()))
                            .sorted()
                            .toList();
                    return new BusArrival(shuttle.name(), API_NAME, arrivals);
                });
    }
}