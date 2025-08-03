package com.example.springbackend.service;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OneMapService {

    private static final Logger log = LoggerFactory.getLogger(OneMapService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${onemap.token}")
    private String oneMapToken;

    public OneMapService(WebClient.Builder webClientBuilder,
                         @Value("${onemap.base-url:https://www.onemap.gov.sg}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Calls the OneMap API, retrieves the raw JSON, and maps it to our simplified DTO.
     *
     * @param start The start location coordinates.
     * @param end The end location coordinates.
     * @return A Mono containing the simplified DirectionsResponseDTO.
     */
    public Mono<DirectionsResponseDTO> getBusRoutes(String start, String end) {
        log.debug("getBusRoutes called with parameters:");
        log.debug("start = '{}'", start);
        log.debug("end = '{}'", end);
        log.debug("date = '{}'", "08-01-2025");
        log.debug("time = '{}'", "07:35:00");
        log.debug("numItineraries = '{}'", "3");
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/public/routingsvc/route")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("routeType", "pt")
                        .queryParam("mode", "BUS")
                        .queryParam("date", "08-01-2025")
                        .queryParam("time", "07:35:00")
                        .queryParam("numItineraries", "3")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, oneMapToken)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.debug("Received raw routing response: {}", response))
                .doOnError(e -> log.error("Error fetching routing data", e))
                .map(this::parseAndMapResponse);
    }

    /**
     * Parses the raw OneMap JSON string and maps it to a DirectionsResponseDTO.
     * This is where the core data extraction logic happens.
     */
    private DirectionsResponseDTO parseAndMapResponse(String jsonResponse) {
        log.debug("Parsing routing JSON response");
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode planNode = root.path("plan");

            if (planNode.isMissingNode() || planNode.isNull()) {
                log.warn("No plan node found in routing response");
                return new DirectionsResponseDTO("Origin", "Destination", List.of());
            }

            String startLoc = planNode.path("from").path("name").asText("Origin");
            String endLoc = planNode.path("to").path("name").asText("Destination");

            JsonNode itinerariesNode = planNode.path("itineraries");
            List<DirectionsResponseDTO.RouteDTO> routes = new ArrayList<>();

            if (itinerariesNode.isArray()) {
                for (int i = 0; i < itinerariesNode.size() && i < 3; i++) {
                    JsonNode itineraryNode = itinerariesNode.get(i);

                    int durationInMinutes = itineraryNode.path("duration").asInt(0) / 60;
                    JsonNode legsNode = itineraryNode.path("legs");
                    List<DirectionsResponseDTO.LegDTO> legs = new ArrayList<>();
                    StringBuilder summaryBuilder = new StringBuilder();
                    StringBuilder routeGeometryBuilder = new StringBuilder();

                    if (legsNode.isArray()) {
                        for (JsonNode legNode : legsNode) {
                            String type = legNode.path("mode").asText("UNKNOWN");
                            int legDuration = legNode.path("duration").asInt(0) / 60;
                            String busServiceNumber = legNode.path("routeShortName").asText(null);

                            String instruction = String.format(
                                    "%s from %s to %s",
                                    type,
                                    legNode.path("from").path("name").asText("a location"),
                                    legNode.path("to").path("name").asText("a location")
                            );

                            legs.add(new DirectionsResponseDTO.LegDTO(type, legDuration, busServiceNumber, instruction));

                            if ("BUS".equals(type) && summaryBuilder.isEmpty()) {
                                summaryBuilder.append("Bus Service ").append(busServiceNumber);
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
                            summaryBuilder.toString(),
                            routeGeometryBuilder.toString()
                    ));
                }
            }

            log.debug("Parsed {} routes from routing response", routes.size());
            return new DirectionsResponseDTO(startLoc, endLoc, routes);

        } catch (Exception e) {
            log.error("Failed to parse routing JSON response", e);
            return new DirectionsResponseDTO("Origin", "Destination", List.of());
        }
    }
}
