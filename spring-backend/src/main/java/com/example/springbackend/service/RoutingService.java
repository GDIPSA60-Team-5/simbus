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

    public Mono<List<DirectionsResponseDTO.RouteDTO>> getBusRoutes(String start, String end, LocalTime arrivalTime, LocalTime startTime) {
        return validateCoordinates(start, end)
                .then(
                        webClient.get()
                                .uri(uriBuilder -> {
                                    LocalDate today = LocalDate.now(SINGAPORE);
                                    LocalTime timeParam = startTime != null ? startTime : LocalTime.now(SINGAPORE);
                                    String dateParam = today.format(DATE_FMT);
                                    String timeStr = timeParam.format(TIME_FMT);
                                    return uriBuilder
                                            .path("/api/public/routingsvc/route")
                                            .queryParam("start", start)
                                            .queryParam("end", end)
                                            .queryParam("routeType", "pt")
                                            .queryParam("mode", "BUS")
                                            .queryParam("date", dateParam)
                                            .queryParam("time", timeStr)
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

                    // Filter routes that arrive before the deadline
                    List<DirectionsResponseDTO.RouteDTO> filteredRoutes = routes.stream()
                            .filter(route -> {
                                LocalDateTime arrivalDateTime = nowDateTime.plusMinutes(route.durationInMinutes());
                                return !arrivalDateTime.isAfter(deadline);
                            })
                            .collect(Collectors.toList());
                    
                    // Log for debugging
                    log.debug("Original routes: {}, Filtered routes: {}, Deadline: {}", 
                             routes.size(), filteredRoutes.size(), deadline);
                    
                    // If no routes meet the deadline, return all routes anyway
                    return filteredRoutes.isEmpty() ? routes : filteredRoutes;
                });
    }

    private List<DirectionsResponseDTO.RouteDTO> parseRoutesOnly(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode planNode = root.path("plan");

            if (planNode.isMissingNode() || planNode.isNull()) {
                log.warn("No plan node found in OneMap response");
                return Collections.emptyList();
            }

            JsonNode itinerariesNode = planNode.path("itineraries");
            if (!itinerariesNode.isArray()) {
                log.warn("No itineraries array found in OneMap response");
                return Collections.emptyList();
            }
            
            log.debug("Found {} itineraries in OneMap response", itinerariesNode.size());
            List<DirectionsResponseDTO.RouteDTO> routes = parseRoutes(itinerariesNode);
            log.debug("Parsed {} routes from itineraries", routes.size());
            
            return routes;

        } catch (Exception e) {
            log.error("Failed to parse routing JSON response", e);
            return Collections.emptyList();
        }
    }

    private List<DirectionsResponseDTO.RouteDTO> parseRoutes(JsonNode itinerariesNode) {
        if (!itinerariesNode.isArray()) return Collections.emptyList();

        List<DirectionsResponseDTO.RouteDTO> routes = new ArrayList<>();

        for (int i = 0; i < Math.min(itinerariesNode.size(), MAX_ITINERARIES); i++) {
            JsonNode itineraryNode = itinerariesNode.get(i);
            int durationInMinutes = itineraryNode.path("duration").asInt(0) / 60;
            List<DirectionsResponseDTO.LegDTO> legs = parseLegs(itineraryNode.path("legs"));

            // Create a comprehensive summary showing all bus services
            List<String> busServices = legs.stream()
                    .filter(leg -> "BUS".equalsIgnoreCase(leg.type()) && leg.busServiceNumber() != null)
                    .map(DirectionsResponseDTO.LegDTO::busServiceNumber)
                    .distinct()
                    .collect(Collectors.toList());
            
            String summary;
            if (busServices.isEmpty()) {
                summary = "Walking route";
            } else if (busServices.size() == 1) {
                summary = "Bus " + busServices.get(0);
            } else {
                summary = "Bus " + String.join(" → ", busServices);
            }

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
            List<Coordinates> routePoints = extractRoutePoints(legNode);
            
            // Extract bus stop details
            String fromStopName = legNode.path("from").path("name").asText(null);
            String fromStopCode = legNode.path("from").path("stopCode").asText(null);
            String toStopName = legNode.path("to").path("name").asText(null);
            String toStopCode = legNode.path("to").path("stopCode").asText(null);

            legs.add(new DirectionsResponseDTO.LegDTO(type, legDuration, busServiceNumber, instruction, legGeometry, routePoints,
                    fromStopName, fromStopCode, toStopName, toStopCode));
        }
        return legs;
    }

    private List<Coordinates> extractRoutePoints(JsonNode legNode) {
        List<Coordinates> routePoints = new ArrayList<>();
        
        // Add starting point
        JsonNode fromNode = legNode.path("from");
        if (!fromNode.isMissingNode()) {
            double lat = fromNode.path("lat").asDouble(0.0);
            double lon = fromNode.path("lon").asDouble(0.0);
            if (lat != 0.0 || lon != 0.0) {
                routePoints.add(new Coordinates(lat, lon));
            }
        }
        
        // Add intermediate stops for transit legs
        JsonNode intermediateStops = legNode.path("intermediateStops");
        if (intermediateStops.isArray()) {
            for (JsonNode stop : intermediateStops) {
                double lat = stop.path("lat").asDouble(0.0);
                double lon = stop.path("lon").asDouble(0.0);
                if (lat != 0.0 || lon != 0.0) {
                    routePoints.add(new Coordinates(lat, lon));
                }
            }
        }
        
        // Add ending point
        JsonNode toNode = legNode.path("to");
        if (!toNode.isMissingNode()) {
            double lat = toNode.path("lat").asDouble(0.0);
            double lon = toNode.path("lon").asDouble(0.0);
            if (lat != 0.0 || lon != 0.0) {
                routePoints.add(new Coordinates(lat, lon));
            }
        }
        
        return routePoints;
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

