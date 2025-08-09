package com.example.springbackend.service;

import com.example.springbackend.controller.GeocodeController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class ReverseGeocodeService {

    private static final Logger log = LoggerFactory.getLogger(ReverseGeocodeService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${onemap.token}")
    private String oneMapToken;

    public ReverseGeocodeService(@Qualifier("oneMapWebClient") WebClient webClient) {
        this.webClient = webClient;
    }
    /**
     * Reverse geocode using SVY21 coordinates.
     *
     * @param x       X coordinate in SVY21 format (e.g., "24291.97788882387")
     * @param y       Y coordinate in SVY21 format (e.g., "31373.0117224489")
     * @return Mono emitting a formatted address string, or empty if none found.
     */
    public Mono<List<GeocodeController.ReverseGeocodeCandidate>> getCandidates(
            String x, String y) {

        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/api/public/revgeocodexy")
                            .queryParam("location", x + "," + y)
                            .queryParam("buffer", "40")
                            .queryParam("addressType", "All")
                            .queryParam("otherFeatures", "N");


                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, oneMapToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new RuntimeException("Reverse geocoding API client error: " + resp.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new RuntimeException("Reverse geocoding API server error: " + resp.statusCode())))
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .map(this::parseReverseGeocodeCandidates)
                .doOnError(e -> log.warn("Error during reverse geocoding for coords {}, {}: {}", x, y, e.getMessage()))
                .onErrorResume(e -> Mono.just(List.of())); // empty list on error
    }

    private List<GeocodeController.ReverseGeocodeCandidate> parseReverseGeocodeCandidates(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode geocodeInfo = root.path("GeocodeInfo");
            if (geocodeInfo.isArray() && !geocodeInfo.isEmpty()) {
                return StreamSupport.stream(geocodeInfo.spliterator(), false)
                        .map(node -> {
                            String building = node.path("BUILDINGNAME").asText("");
                            String block = node.path("BLOCK").asText("");
                            String road = node.path("ROAD").asText("");
                            String postalCode = node.path("POSTALCODE").asText("");
                            StringBuilder address = new StringBuilder();
                            if (!building.isBlank()) address.append(building).append(", ");
                            if (!block.isBlank()) address.append("Block ").append(block).append(", ");
                            if (!road.isBlank()) address.append(road).append(", ");
                            if (!postalCode.isBlank()) address.append("Singapore ").append(postalCode);
                            String formatted = address.toString().replaceAll(", $", "");
                            return new GeocodeController.ReverseGeocodeCandidate(formatted);
                        })
                        .toList();
            }
        } catch (Exception e) {
            log.error("Failed to parse reverse geocode candidates", e);
        }
        return List.of();
    }
}
