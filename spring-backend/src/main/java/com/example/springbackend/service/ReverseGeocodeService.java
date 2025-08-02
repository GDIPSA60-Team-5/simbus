package com.example.springbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ReverseGeocodeService {

    private static final Logger log = LoggerFactory.getLogger(ReverseGeocodeService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${onemap.token}")
    private String oneMapToken;

    public ReverseGeocodeService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                                 @Value("${onemap.base-url:https://www.onemap.gov.sg}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    /**
     * Reverse geocode using SVY21 coordinates.
     *
     * @param xCoord       X coordinate in SVY21 format (e.g., "24291.97788882387")
     * @param yCoord       Y coordinate in SVY21 format (e.g., "31373.0117224489")
     * @param buffer       Optional buffer radius in meters (0-500)
     * @param addressType  Optional address type filter ("HDB" or "All")
     * @param otherFeatures Optional flag for other features ("Y" or "N")
     * @return Mono emitting a formatted address string, or empty if none found.
     */
    public Mono<String> reverseGeocode(String xCoord, String yCoord,
                                       String buffer, String addressType, String otherFeatures) {
        if (xCoord == null || xCoord.isBlank() || yCoord == null || yCoord.isBlank()) {
            return Mono.error(new IllegalArgumentException("X and Y coordinates must not be blank"));
        }

        if (oneMapToken == null || oneMapToken.isBlank()) {
            log.warn("OneMap token is not configured");
        }

        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/api/public/revgeocodexy")
                            .queryParam("location", xCoord + "," + yCoord);

                    if (buffer != null && !buffer.isBlank()) builder.queryParam("buffer", buffer);
                    if (addressType != null && !addressType.isBlank()) builder.queryParam("addressType", addressType);
                    if (otherFeatures != null && !otherFeatures.isBlank()) builder.queryParam("otherFeatures", otherFeatures);

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
                .mapNotNull(this::parseResponse)
                .doOnError(e -> log.warn("Error during reverse geocoding for coords {}, {}: {}", xCoord, yCoord, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private String parseResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode geocodeInfo = root.path("GeocodeInfo");
            if (geocodeInfo.isArray() && !geocodeInfo.isEmpty()) {
                JsonNode first = geocodeInfo.get(0);

                String building = first.path("BUILDINGNAME").asText("");
                String block = first.path("BLOCK").asText("");
                String road = first.path("ROAD").asText("");
                String postalCode = first.path("POSTALCODE").asText("");

                StringBuilder address = new StringBuilder();
                if (!building.isBlank()) address.append(building).append(", ");
                if (!block.isBlank()) address.append("Block ").append(block).append(", ");
                if (!road.isBlank()) address.append(road).append(", ");
                if (!postalCode.isBlank()) address.append("Singapore ").append(postalCode);

                return address.toString().replaceAll(", $", "");
            }
        } catch (Exception e) {
            log.error("Failed to parse reverse geocode response", e);
        }
        return null;
    }
}
