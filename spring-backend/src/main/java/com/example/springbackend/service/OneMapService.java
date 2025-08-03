package com.example.springbackend.service;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OneMapService {

    private static final Logger log = LoggerFactory.getLogger(OneMapService.class);
    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String DEFAULT_START = "1.3048227829575314,103.76929361060819";
    private static final int MAX_ITINERARIES = 3;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${onemap.token}")
    private String oneMapToken;

    public OneMapService(WebClient.Builder webClientBuilder,
                         @Value("${onemap.base-url:https://www.onemap.gov.sg}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<DirectionsResponseDTO> getBusRoutes(String start,
                                                    String end,
                                                    LocalTime arrivalTime) {
        String effectiveStart = (start == null || start.isBlank()) ? DEFAULT_START : start;

        LocalDate today = LocalDate.now(SINGAPORE);
        LocalTime now = LocalTime.now(SINGAPORE);
        String dateParam = today.format(DATE_FMT);
        String timeParam = now.format(TIME_FMT);
        LocalDateTime nowDateTime = LocalDateTime.of(today, now);
        LocalDateTime deadline = (arrivalTime != null) ? LocalDateTime.of(today, arrivalTime) : null;
        URI uri = UriComponentsBuilder.fromUriString("https://www.onemap.gov.sg/api/public/routingsvc/route")
                .queryParam("start", effectiveStart)
                .queryParam("end", end)
                .queryParam("routeType", "pt")
                .queryParam("mode", "BUS,WALK")
                .queryParam("date", dateParam)
                .queryParam("time", timeParam)
                .queryParam("numItineraries", MAX_ITINERARIES)
                .build()
                .toUri();

        log.info("Constructed URI: {}", uri);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/public/routingsvc/route")
                        .queryParam("start", effectiveStart)
                        .queryParam("end", end)
                        .queryParam("routeType", "pt")
                        .queryParam("mode", "BUS")
                        .queryParam("date", dateParam)
                        .queryParam("time", timeParam)
                        .queryParam("numItineraries", String.valueOf(MAX_ITINERARIES))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, oneMapToken)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.debug("Raw routing response: {}", response))
                .doOnError(e -> log.error("Error fetching routing data", e))
                .map(this::parseAndMapResponse)
                .map(dto -> {
                    if (deadline != null) {
                        List<DirectionsResponseDTO.RouteDTO> filtered = dto.suggestedRoutes().stream()
                                .filter(route -> {
                                    LocalDateTime projectedArrival = nowDateTime.plusMinutes(route.durationInMinutes());
                                    return !projectedArrival.isAfter(deadline);
                                })
                                .collect(Collectors.toList());
                        return new DirectionsResponseDTO(dto.startLocation(), dto.endLocation(), filtered);
                    }
                    return dto;
                });
    }

    private DirectionsResponseDTO parseAndMapResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode planNode = root.path("plan");

            if (planNode.isMissingNode() || planNode.isNull()) {
                return new DirectionsResponseDTO("Origin", "Destination", List.of());
            }

            String startLoc = planNode.path("from").path("name").asText("Origin");
            String endLoc = planNode.path("to").path("name").asText("Destination");

            JsonNode itinerariesNode = planNode.path("itineraries");

            List<DirectionsResponseDTO.RouteDTO> routes = new ArrayList<>();

            if (itinerariesNode.isArray()) {
                for (int i = 0; i < Math.min(itinerariesNode.size(), MAX_ITINERARIES); i++) {
                    JsonNode itineraryNode = itinerariesNode.get(i);
                    int durationInMinutes = itineraryNode.path("duration").asInt(0) / 60;
                    JsonNode legsNode = itineraryNode.path("legs");

                    List<DirectionsResponseDTO.LegDTO> legs = new ArrayList<>();
                    String summary = "";
                    StringBuilder routeGeometryBuilder = new StringBuilder();

                    if (legsNode.isArray()) {
                        for (JsonNode legNode : legsNode) {
                            String type = legNode.path("mode").asText("UNKNOWN");
                            int legDuration = legNode.path("duration").asInt(0) / 60;
                            String busServiceNumber = legNode.path("routeShortName").asText(null);

                            String fromName = legNode.path("from").path("name").asText("a location");
                            String toName = legNode.path("to").path("name").asText("a location");
                            String instruction = String.format("%s from %s to %s", type, fromName, toName);

                            legs.add(new DirectionsResponseDTO.LegDTO(type, legDuration, busServiceNumber, instruction));

                            if (summary.isEmpty() && "BUS".equals(type) && busServiceNumber != null) {
                                summary = "Bus Service " + busServiceNumber;
                            }

                            String geometry = legNode.path("legGeometry").path("points").asText(null);
                            if (geometry != null) {
                                routeGeometryBuilder.append(geometry);
                            }
                        }
                    }

                    routes.add(new DirectionsResponseDTO.RouteDTO(
                            durationInMinutes,
                            legs,
                            summary,
                            routeGeometryBuilder.toString()
                    ));
                }
            }

            return new DirectionsResponseDTO(startLoc, endLoc, routes);

        } catch (Exception e) {
            log.error("Failed to parse routing JSON response", e);
            return new DirectionsResponseDTO("Origin", "Destination", List.of());
        }
    }
}
