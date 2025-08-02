package com.example.springbackend.controller;

import com.example.springbackend.dto.*;
import com.example.springbackend.service.DirectionService;
import com.example.springbackend.service.GeocodingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ChatbotControllerV1 {

    private static final Logger log = LoggerFactory.getLogger(ChatbotControllerV1.class);

    private final DirectionService oneMapService;
    private final GeocodingService geocodingService;

    public ChatbotControllerV1(DirectionService oneMapService, GeocodingService geocodingService) {
        this.oneMapService = oneMapService;
        this.geocodingService = geocodingService;
    }

    @PostMapping("/api/v1/chatbot")
    public Mono<BotResponseDTO> handleChatInput(@RequestBody ChatRequest request) {
        String input = request.userInput();
        if (input == null) input = "";
        input = input.trim();

        log.debug("Received chat input: '{}', currentLocation: {}", input,
                request.currentLocation() != null ? formatCoords(request.currentLocation()) : "none");

        // greeting detection
        if (input.matches("(?i)\\b(hi|hello|hey|good\\s+morning|good\\s+afternoon|good\\s+evening)\\b.*")) {
            log.debug("Detected greeting, replying with welcome message.");
            return Mono.just(new MessageResponseDTO("Hello there! How can I help you today?"));
        }

        String startLocationName = null;
        String endLocationName = null;

        // Extract "from <location>"
        Pattern fromPattern = Pattern.compile("(?i)\\bfrom\\s+([A-Za-z ]+?)(?=\\s+to\\b|[?.!,]|$)");
        Matcher fromMatcher = fromPattern.matcher(input);
        if (fromMatcher.find()) {
            startLocationName = fromMatcher.group(1).trim();
        }

        // Extract "to <location>"
        Pattern toPattern = Pattern.compile("(?i)\\bto\\s+([A-Za-z ]+?)(?=\\s+from\\b|[?.!,]|$)");
        Matcher toMatcher = toPattern.matcher(input);
        if (toMatcher.find()) {
            endLocationName = toMatcher.group(1).trim();
        }

        // loose fallback
        if ((endLocationName == null || endLocationName.isBlank()) && !input.isBlank()) {
            if (!input.matches("(?i)\\b(help|thanks|thank you)\\b.*")) {
                endLocationName = input;
            }
        }

        log.debug("Parsed startLocationName='{}', endLocationName='{}'", startLocationName, endLocationName);

        if (endLocationName == null || endLocationName.isBlank()) {
            if (request.currentLocation() != null) {
                return Mono.just(new MessageResponseDTO(
                        "I didn't catch a destination. Please specify where you want to go, for example: 'to Orchard' or 'from Bishan to Orchard'."));
            } else {
                return Mono.just(new ErrorResponseDTO(
                        "I didn't understand that. Provide a destination (e.g., 'to Orchard') and optionally a start location or your current location."));
            }
        }

        Mono<Coordinates> startCoordsMono;
        if (startLocationName != null && !startLocationName.isBlank()) {
            startCoordsMono = geocodingService.getCoordinates(startLocationName);
        } else if (request.currentLocation() != null) {
            startCoordsMono = Mono.just(request.currentLocation());
        } else {
            return Mono.just(new ErrorResponseDTO(
                    "Start location missing. Please provide a 'from <place>' or ensure your current location is supplied."));
        }

        Mono<Coordinates> endCoordsMono = geocodingService.getCoordinates(endLocationName);

        return Mono.zip(startCoordsMono, endCoordsMono)
                .flatMap(tuple -> {
                    Coordinates start = tuple.getT1();
                    Coordinates end = tuple.getT2();

                    if (start == null || end == null
                            || start.latitude() == null || start.longitude() == null
                            || end.latitude() == null || end.longitude() == null) {
                        log.warn("Failed to resolve one or both locations to coordinates: start={} end={}",
                                start, end);
                        return Mono.just(new ErrorResponseDTO("Could not resolve one or both locations to valid coordinates."));
                    }

                    String startCoords = sanitize(start.latitude()) + "," + sanitize(start.longitude());
                    String endCoords = sanitize(end.latitude()) + "," + sanitize(end.longitude());

                    log.debug("Calling direction service with start='{}' end='{}'", startCoords, endCoords);

                    return oneMapService.getBusRoutes(startCoords, endCoords)
                            .map(directionsDto -> (BotResponseDTO) directionsDto)
                            .onErrorResume(e -> {
                                log.error("Direction service call failed: {}", e.getMessage());
                                return Mono.just(new ErrorResponseDTO("Failed to fetch directions."));
                            });
                })
                .switchIfEmpty(Mono.just(new ErrorResponseDTO(
                        "Sorry, I couldn't find routes between those locations.")));
    }

    private String sanitize(String s) {
        return s == null ? "" : s.trim();
    }

    private String formatCoords(Coordinates c) {
        if (c == null || c.latitude() == null || c.longitude() == null) return "invalid";
        return sanitize(c.latitude()) + "," + sanitize(c.longitude());
    }
}
