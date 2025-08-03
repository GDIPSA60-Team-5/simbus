package com.example.springbackend.service;

import com.example.springbackend.controller.GeocodeController;
import com.example.springbackend.model.Coordinates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Service to convert a location name (e.g., "Orchard") to
 * latitude and longitude coordinates using the OneMap Search API.
 */
@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${onemap.token}")
    private String oneMapToken;


    public GeocodingService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                            @Value("${onemap.base-url:https://www.onemap.gov.sg}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a list of candidates for the location name (not just first result).
     */
    public Mono<List<GeocodeController.GeocodeCandidate>> getCandidates(String locationName) {
        if (locationName == null || locationName.isBlank()) {
            return Mono.error(new IllegalArgumentException("locationName must not be blank"));
        }

        if (oneMapToken == null || oneMapToken.isBlank()) {
            log.warn("OneMap token is not configured");
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/common/elastic/search")
                        .queryParam("searchVal", locationName)
                        .queryParam("returnGeom", "Y")
                        .queryParam("getAddrDetails", "Y")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, oneMapToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new RuntimeException("Geocoding API client error: " + resp.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new RuntimeException("Geocoding API server error: " + resp.statusCode())))
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .map(this::parseGeocodeCandidates)
                .doOnError(e -> log.warn("Error during geocoding for '{}': {}", locationName, e.getMessage()))
                .onErrorResume(e -> Mono.just(List.of())); // return empty list on error
    }

    private List<GeocodeController.GeocodeCandidate> parseGeocodeCandidates(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode results = root.path("results");
            if (results.isArray() && !results.isEmpty()) {
                return StreamSupport.stream(results.spliterator(), false)
                        .map(node -> new GeocodeController.GeocodeCandidate(
                                node.path("LATITUDE").asText(""),
                                node.path("LONGITUDE").asText(""),
                                node.path("SEARCHVAL").asText(""),
                                node.path("POSTAL").asText(""),
                                node.path("BLK_NO").asText(""),
                                node.path("ROAD_NAME").asText(""),
                                node.path("BUILDING").asText("")
                        ))
                        .toList();
            }
        } catch (Exception e) {
            log.error("Failed to parse geocode candidates", e);
        }
        return List.of();
    }
}
