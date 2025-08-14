package com.example.springbackend.service;

import com.example.springbackend.controller.GeocodeController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${onemap.token}")
    private String oneMapToken;


    public GeocodingService(@Qualifier("oneMapWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Returns a list of candidates for the location name (not just first result).
     */
    public Mono<List<GeocodeController.GeocodeCandidate>> getCandidates(String locationName) {
        if (locationName == null || locationName.isBlank()) {
            return Mono.error(new IllegalArgumentException("locationName must not be blank"));
        }

        if (oneMapToken == null || oneMapToken.isBlank()) {
            log.warn("OneMap token is not configured, using fallback data");
            return getFallbackCandidates(locationName);
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
    
    /**
     * Fallback method that returns some common Singapore locations when OneMap token is not available
     */
    private Mono<List<GeocodeController.GeocodeCandidate>> getFallbackCandidates(String locationName) {
        List<GeocodeController.GeocodeCandidate> fallbackLocations = List.of(
            new GeocodeController.GeocodeCandidate(
                "1.2927777312893", "103.854173501417", "CITYLINK MALL", 
                "039393", "1", "RAFFLES LINK", "CITYLINK MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.35435719591377", "103.944305344915", "EASTLINK MALL", 
                "529543", "8", "TAMPINES CENTRAL 1", "EASTLINK MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.37782876616465", "103.942364336974", "ELIAS MALL", 
                "510623", "623", "ELIAS ROAD", "ELIAS MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.28964784521264", "103.856267250977", "ESPLANADE MALL", 
                "039802", "8", "RAFFLES AVENUE", "ESPLANADE MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.37843100827586", "103.762842789877", "HILLION MALL", 
                "678278", "17", "PETIR ROAD", "HILLION MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.37249493810003", "103.893775562605", "HOUGANG MALL", 
                "538766", "90", "HOUGANG AVENUE 10", "HOUGANG MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.31754555939886", "103.785850542515", "JELITA MALL", 
                "278628", "293", "HOLLAND ROAD", "JELITA MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.30540765569962", "103.788446680148", "ROCHESTER MALL", 
                "138639", "35", "ROCHESTER DRIVE", "ROCHESTER MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.35252737278519", "103.944698751072", "TAMPINES MALL", 
                "529510", "4", "TAMPINES CENTRAL 5", "TAMPINES MALL"),
            new GeocodeController.GeocodeCandidate(
                "1.32694504456951", "103.846554377679", "ZHONGSHAN MALL", 
                "329984", "20", "AH HOOD ROAD", "ZHONGSHAN MALL")
        );
        
        // Simple string matching for demo purposes
        List<GeocodeController.GeocodeCandidate> matches = fallbackLocations.stream()
            .filter(candidate -> candidate.displayName().toLowerCase().contains(locationName.toLowerCase()) ||
                               candidate.building().toLowerCase().contains(locationName.toLowerCase()) ||
                               candidate.road().toLowerCase().contains(locationName.toLowerCase()))
            .toList();
            
        return Mono.just(matches);
    }
}
