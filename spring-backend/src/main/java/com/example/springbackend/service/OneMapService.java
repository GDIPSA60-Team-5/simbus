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
public class OneMapService {

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
            System.out.println("OneMap API raw response: " + jsonResponse);  // Log raw response

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode planNode = root.path("plan");

            if (planNode.isMissingNode() || planNode.isNull()) {
                System.out.println("No 'plan' node found in JSON response.");
                return new DirectionsResponseDTO();
            }

            DirectionsResponseDTO responseDto = new DirectionsResponseDTO();
            String startLoc = planNode.path("from").path("name").asText(null);
            String endLoc = planNode.path("to").path("name").asText(null);

            System.out.println("Parsed start location: " + startLoc);
            System.out.println("Parsed end location: " + endLoc);

            responseDto.setStartLocation(startLoc != null ? startLoc : "Origin");
            responseDto.setEndLocation(endLoc != null ? endLoc : "Destination");

            List<RouteDTO> routes = new ArrayList<>();
            JsonNode itinerariesNode = planNode.path("itineraries");

            if (!itinerariesNode.isArray()) {
                System.out.println("'itineraries' node is missing or not an array.");
            } else {
                System.out.println("Found " + itinerariesNode.size() + " itineraries.");

                for (int i = 0; i < itinerariesNode.size() && i < 3; i++) {
                    JsonNode itineraryNode = itinerariesNode.get(i);
                    RouteDTO routeDto = new RouteDTO();
                    routeDto.setDurationInMinutes(itineraryNode.path("duration").asInt(0) / 60);

                    List<LegDTO> legs = new ArrayList<>();
                    JsonNode legsNode = itineraryNode.path("legs");
                    StringBuilder summaryBuilder = new StringBuilder();
                    String routeGeometry = "";

                    if (!legsNode.isArray()) {
                        System.out.println("Itinerary " + i + ": 'legs' node is missing or not an array.");
                    } else {
                        System.out.println("Itinerary " + i + ": found " + legsNode.size() + " legs.");
                        for (JsonNode legNode : legsNode) {
                            LegDTO legDto = new LegDTO();
                            legDto.setType(legNode.path("mode").asText("UNKNOWN"));
                            legDto.setDurationInMinutes(legNode.path("duration").asInt(0) / 60);
                            legDto.setBusServiceNumber(legNode.path("routeShortName").asText(null));

                            String instruction = String.format(
                                    "%s from %s to %s",
                                    legDto.getType(),
                                    legNode.path("from").path("name").asText("a location"),
                                    legNode.path("to").path("name").asText("a location")
                            );
                            legDto.setInstruction(instruction);
                            legs.add(legDto);

                            if ("BUS".equals(legDto.getType()) && summaryBuilder.length() == 0) {
                                summaryBuilder.append("Bus Service ").append(legDto.getBusServiceNumber());
                            }

                            JsonNode geometryNode = legNode.path("legGeometry").path("points");
                            if (geometryNode != null && geometryNode.isTextual()) {
                                routeGeometry += geometryNode.asText();
                            }
                        }
                    }
                    routeDto.setLegs(legs);
                    routeDto.setSummary(summaryBuilder.toString());
                    routeDto.setRouteGeometry(routeGeometry);
                    routes.add(routeDto);
                }
            }
            responseDto.setSuggestedRoutes(routes);

            System.out.println("Returning DirectionsResponseDTO with " + routes.size() + " routes.");
            return responseDto;

        } catch (Exception e) {
            System.err.println("Exception during parseAndMapResponse:");
            e.printStackTrace();
            return new DirectionsResponseDTO();
        }
    }
}