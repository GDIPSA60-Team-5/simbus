package com.example.springbackend.service;

import com.example.springbackend.dto.Coordinates;
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
     * Finds the coordinates for a given location name.
     *
     * @param locationName The name of the location to search for.
     * @return A Mono containing Coordinates if found, or empty if none / on failure.
     */
    public Mono<Coordinates> getCoordinates(String locationName) {
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
                .map(this::parseGeocodingResponse)
                .flatMap(coords -> {
                    if (coords.latitude() != null && coords.longitude() != null) {
                        return Mono.just(coords);
                    } else {
                        return Mono.empty();
                    }
                })
                .doOnError(e -> log.warn("Error during geocoding for '{}': {}", locationName, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Parses the raw JSON response from the OneMap Search API
     * and extracts the latitude and longitude of the first result.
     */
    private Coordinates parseGeocodingResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode results = root.path("results");
            if (results.isArray() && !results.isEmpty()) {
                JsonNode first = results.get(0);
                String lat = first.path("LATITUDE").asText(null);
                String lon = first.path("LONGITUDE").asText(null);
                if (lat != null && lon != null && !lat.isBlank() && !lon.isBlank()) {
                    return new Coordinates(lat, lon);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse geocoding response", e);
        }
        return new Coordinates(null, null);
    }
}
