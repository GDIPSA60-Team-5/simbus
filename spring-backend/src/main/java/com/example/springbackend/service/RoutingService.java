package com.example.springbackend.service;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.model.Coordinates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final int MAX_ITINERARIES = 3;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${onemap.token}")
    private String oneMapToken;

    public RoutingService(@Qualifier("oneMapWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<List<DirectionsResponseDTO.RouteDTO>> getBusRoutes(String start, String end, LocalTime arrivalTime) {
        return validateCoordinates(start, end)
                .then(
                        webClient.get()
                                .uri(uriBuilder -> {
                                    LocalDate today = LocalDate.now(SINGAPORE);
                                    LocalTime now = LocalTime.now(SINGAPORE);
                                    String dateParam = today.format(DATE_FMT);
                                    String timeParam = now.format(TIME_FMT);
                                    return uriBuilder
                                            .path("/api/public/routingsvc/route")
                                            .queryParam("start", start)
                                            .queryParam("end", end)
                                            .queryParam("routeType", "pt")
                                            .queryParam("mode", "BUS")
                                            .queryParam("date", dateParam)
                                            .queryParam("time", timeParam)
                                            .queryParam("numItineraries", MAX_ITINERARIES)
                                            .build();
                                })
                                .header(HttpHeaders.AUTHORIZATION, oneMapToken)
                                .retrieve()
                                .bodyToMono(String.class)
                                .doOnNext(response -> log.debug("Raw routing response: {}", response))
                                .doOnError(e -> log.error("Error fetching routing data", e))
                )
                .map(this::parseRoutesOnly)
                .map(routes -> {
                    if (arrivalTime == null) return routes;

                    LocalDateTime nowDateTime = LocalDateTime.of(LocalDate.now(SINGAPORE), LocalTime.now(SINGAPORE));
                    LocalDateTime deadline = LocalDateTime.of(LocalDate.now(SINGAPORE), arrivalTime);

                    return routes.stream()
                            .filter(route -> !nowDateTime.plusMinutes(route.durationInMinutes()).isAfter(deadline))
                            .collect(Collectors.toList());
                });
    }

    private List<DirectionsResponseDTO.RouteDTO> parseRoutesOnly(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode planNode = root.path("plan");

            if (planNode.isMissingNode() || planNode.isNull()) {
                return Collections.emptyList();
            }

            Coordinates dummyStartCoordinates = new Coordinates(0.0, 0.0);
            Coordinates dummyEndCoordinates = new Coordinates(0.0, 0.0);

            return parseRoutes(planNode.path("itineraries"), dummyStartCoordinates, dummyEndCoordinates);

        } catch (Exception e) {
            log.error("Failed to parse routing JSON response", e);
            return Collections.emptyList();
        }
    }

    private List<DirectionsResponseDTO.RouteDTO> parseRoutes(JsonNode itinerariesNode, Coordinates startCoordinates, Coordinates endCoordinates) {
        if (!itinerariesNode.isArray()) return Collections.emptyList();

        List<DirectionsResponseDTO.RouteDTO> routes = new ArrayList<>();

        for (int i = 0; i < Math.min(itinerariesNode.size(), MAX_ITINERARIES); i++) {
            JsonNode itineraryNode = itinerariesNode.get(i);
            int durationInMinutes = itineraryNode.path("duration").asInt(0) / 60;
            List<DirectionsResponseDTO.LegDTO> legs = parseLegs(itineraryNode.path("legs"));

            String summary = legs.stream()
                    .filter(leg -> "BUS".equalsIgnoreCase(leg.type()) && leg.busServiceNumber() != null)
                    .findFirst()
                    .map(leg -> "Bus Service " + leg.busServiceNumber())
                    .orElse("");

            routes.add(new DirectionsResponseDTO.RouteDTO(
                    durationInMinutes,
                    legs,
                    summary
            ));
        }
        return routes;
    }

    private List<DirectionsResponseDTO.LegDTO> parseLegs(JsonNode legsNode) {
        if (!legsNode.isArray()) return Collections.emptyList();

        List<DirectionsResponseDTO.LegDTO> legs = new ArrayList<>();

        for (JsonNode legNode : legsNode) {
            String type = legNode.path("mode").asText("UNKNOWN");
            int legDuration = legNode.path("duration").asInt(0) / 60;
            String busServiceNumber = legNode.path("routeShortName").asText(null);

            String fromName = legNode.path("from").path("name").asText("a location");
            String toName = legNode.path("to").path("name").asText("a location");
            String instruction = String.format("%s from %s to %s", type, fromName, toName);

            String legGeometry = legNode.path("legGeometry").path("points").asText(null);

            legs.add(new DirectionsResponseDTO.LegDTO(type, legDuration, busServiceNumber, instruction, legGeometry));
        }
        return legs;
    }

    private Mono<Void> validateCoordinates(String start, String end) {
        if (start == null || start.isBlank()) {
            return Mono.error(new IllegalArgumentException("Start coordinate is required"));
        }
        if (end == null || end.isBlank()) {
            return Mono.error(new IllegalArgumentException("End coordinate is required"));
        }
        if (!isValidLatLon(start)) {
            return Mono.error(new IllegalArgumentException("Invalid start coordinate format"));
        }
        if (!isValidLatLon(end)) {
            return Mono.error(new IllegalArgumentException("Invalid end coordinate format"));
        }
        return Mono.empty();
    }

    private boolean isValidLatLon(String latLon) {
        Coordinates coords = Coordinates.fromString(latLon);
        double lat = coords.latitude();
        double lon = coords.longitude();

        // If fromString returned default 0.0,0.0, reject unless input was exactly "0,0"
        if ((lat == 0.0 && lon == 0.0) && !latLon.trim().equals("0,0")) {
            return false;
        }

        if (lat < -90 || lat > 90) return false;
        if (lon < -180 || lon > 180) return false;

        return true;
    }
}

