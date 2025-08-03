package com.example.springbackend.service.implementation;

import com.example.springbackend.dto.llm.BotResponseDTO;
import com.example.springbackend.dto.llm.ErrorResponseDTO;
import com.example.springbackend.dto.llm.MessageResponseDTO;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.service.ChatbotService;
import com.example.springbackend.service.GeocodingService;
import com.example.springbackend.service.OneMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LocalChatbotService implements ChatbotService {
    private final OneMapService oneMapService;
    private final GeocodingService geocodingService;
    private static final Logger log = LoggerFactory.getLogger(LocalChatbotService.class);

    public LocalChatbotService(OneMapService oneMapService, GeocodingService geocodingService) {
        this.oneMapService = oneMapService;
        this.geocodingService = geocodingService;
    }

    @Override
    public Mono<BotResponseDTO> handleChatInput(ChatRequest request, HttpHeaders incomingHeaders) {
        String input = request.userInput();
        if (input == null) input = "";
        input = input.trim();

        if (input.matches("(?i)\\b(hi|hello|hey|good\\s+morning|good\\s+afternoon|good\\s+evening)\\b.*")) {
            return Mono.just(new MessageResponseDTO("Hello there! How can I help you today?"));
        }

        String startLocationName = null;
        String endLocationName = null;

        Pattern fromPattern = Pattern.compile("(?i)\\bfrom\\s+([A-Za-z ]+?)(?=\\s+to\\b|[?.!,]|$)");
        Matcher fromMatcher = fromPattern.matcher(input);
        if (fromMatcher.find()) {
            startLocationName = fromMatcher.group(1).trim();
        }

        Pattern toPattern = Pattern.compile("(?i)\\bto\\s+([A-Za-z ]+?)(?=\\s+from\\b|[?.!,]|$)");
        Matcher toMatcher = toPattern.matcher(input);
        if (toMatcher.find()) {
            endLocationName = toMatcher.group(1).trim();
        }

        if ((endLocationName == null || endLocationName.isBlank()) && !input.isBlank()) {
            if (!input.matches("(?i)\\b(help|thanks|thank you)\\b.*")) {
                endLocationName = input;
            }
        }

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

                    if (start.latitude() == null || start.longitude() == null || end.latitude() == null || end.longitude() == null) {
                        return Mono.just(new ErrorResponseDTO("Could not resolve one or both locations to valid coordinates."));
                    }

                    String startCoords = sanitize(start.latitude()) + "," + sanitize(start.longitude());
                    String endCoords = sanitize(end.latitude()) + "," + sanitize(end.longitude());

                    return oneMapService.getBusRoutes(startCoords, endCoords)
                            .map(directionsDto -> (BotResponseDTO) directionsDto)
                            .onErrorResume(e -> Mono.just(new ErrorResponseDTO("Failed to fetch directions.")));
                })
                .switchIfEmpty(Mono.just(new ErrorResponseDTO(
                        "Sorry, I couldn't find routes between those locations.")));
    }

    private String sanitize(String s) {
        return s == null ? "" : s.trim();
    }
}
