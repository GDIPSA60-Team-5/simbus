package com.example.springbackend.service;

import com.example.springbackend.dto.DirectionsResponseDTO;
import com.example.springbackend.dto.LegDTO;
import com.example.springbackend.dto.RouteDTO;
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

@Service
public class DirectionService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${onemap.token}")
    private String oneMapToken;

    public DirectionService(WebClient.Builder webClientBuilder,
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
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/public/routingsvc/route")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("routeType", "pt")
                        .queryParam("mode", "BUS")
                        .queryParam("date", "08-01-2025") // Hardcoded for this example
                        .queryParam("time", "07:35:00") // Hardcoded for this example
                        .queryParam("numItineraries", "3") // Hardcoded for this example
                        .build())
                .header(HttpHeaders.AUTHORIZATION, oneMapToken)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseAndMapResponse);
    }

    /**
     * Parses the raw OneMap JSON string and maps it to a DirectionsResponseDTO.
     * This is where the core data extraction logic happens.
     */
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
            List<RouteDTO> routes = new ArrayList<>();

            if (itinerariesNode.isArray()) {
                for (int i = 0; i < itinerariesNode.size() && i < 3; i++) {
                    JsonNode itineraryNode = itinerariesNode.get(i);

                    int durationInMinutes = itineraryNode.path("duration").asInt(0) / 60;
                    JsonNode legsNode = itineraryNode.path("legs");
                    List<LegDTO> legs = new ArrayList<>();
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

                            legs.add(new LegDTO(type, legDuration, busServiceNumber, instruction));

                            if ("BUS".equals(type) && summaryBuilder.length() == 0) {
                                summaryBuilder.append("Bus Service ").append(busServiceNumber);
                            }

                            String geometry = legNode.path("legGeometry").path("points").asText(null);
                            if (geometry != null) {
                                routeGeometryBuilder.append(geometry);
                            }
                        }
                    }

                    routes.add(new RouteDTO(
                            durationInMinutes,
                            legs,
                            summaryBuilder.toString(),
                            routeGeometryBuilder.toString()
                    ));
                }
            }

            return new DirectionsResponseDTO(startLoc, endLoc, routes);

        } catch (Exception e) {
            e.printStackTrace();
            return new DirectionsResponseDTO("Origin", "Destination", List.of());
        }
    }

}