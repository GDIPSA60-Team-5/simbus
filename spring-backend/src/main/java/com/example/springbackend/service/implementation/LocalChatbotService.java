package com.example.springbackend.service.implementation;

import com.example.springbackend.controller.GeocodeController; // Required import
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@ConditionalOnProperty(name = "chatbot.strategy", havingValue = "local", matchIfMissing = true)
public class LocalChatbotService implements ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(LocalChatbotService.class);

    private static final Pattern GREETING = Pattern.compile("(?i)\\b(hi|hello|hey|good\\s+morning|good\\s+afternoon|good\\s+evening)\\b");
    private static final Pattern FROM_TO_PATTERN = Pattern.compile("(?i)\\bfrom\\s+([A-Za-z0-9 ]+?)\\s+to\\s+([A-Za-z0-9 ]+?)\\b");
    private static final Pattern FROM_PATTERN = Pattern.compile("(?i)\\bfrom\\s+([A-Za-z0-9 ]+?)(?=\\s+to\\b|[?.!,]|$)");
    private static final Pattern TO_PATTERN = Pattern.compile("(?i)\\bto\\s+([A-Za-z0-9 ]+?)(?=\\s+from\\b|[?.!,]|$)");
    private static final Pattern IGNORE_INPUT = Pattern.compile("(?i)\\b(help|thanks|thank you|please)\\b");


    private final OneMapService oneMapService;
    private final GeocodingService geocodingService;

    public LocalChatbotService(OneMapService oneMapService, GeocodingService geocodingService) {
        this.oneMapService = oneMapService;
        this.geocodingService = geocodingService;
    }

    @Override
    public Mono<BotResponseDTO> handleChatInput(ChatRequest request, HttpHeaders incomingHeaders) {
        String input = safeTrim(request.userInput());

        if (input.isEmpty()) {
            return Mono.just(new ErrorResponseDTO("Empty input. Please tell me where you want to go, e.g., 'from Bishan to Orchard'."));
        }

        if (GREETING.matcher(input).find()) {
            return Mono.just(new MessageResponseDTO("Hello there! How can I help you today?"));
        }

        String startLocationName = null;
        String endLocationName = null;

        // Try full "from X to Y"
        Matcher ftMatcher = FROM_TO_PATTERN.matcher(input);
        if (ftMatcher.find()) {
            startLocationName = ftMatcher.group(1).trim();
            endLocationName = ftMatcher.group(2).trim();
        } else {
            Matcher fromMatcher = FROM_PATTERN.matcher(input);
            if (fromMatcher.find()) {
                startLocationName = fromMatcher.group(1).trim();
            }
            Matcher toMatcher = TO_PATTERN.matcher(input);
            if (toMatcher.find()) {
                endLocationName = toMatcher.group(1).trim();
            }
        }

        // Fallback: If no explicit "to" and input isn't a trivial command, treat entire input as destination
        if ((endLocationName == null || endLocationName.isBlank())
                && !IGNORE_INPUT.matcher(input).find()) {
            String cleanInput = IGNORE_INPUT.matcher(input).replaceAll("").trim();
            if (!cleanInput.isBlank()) {
                endLocationName = cleanInput;
            }
        }

        if (endLocationName == null || endLocationName.isBlank()) {
            if (request.currentLocation() != null) {
                return Mono.just(new MessageResponseDTO(
                        "I didn't catch a destination. Please specify where you want to go, e.g., 'to Orchard' or 'from Bishan to Orchard'."));
            } else {
                return Mono.just(new ErrorResponseDTO(
                        "Destination missing. Provide a destination (e.g., 'to Orchard') and optionally a start location or supply your current location."));
            }
        }

        Mono<Coordinates> startCoordsMono;
        if (startLocationName != null && !startLocationName.isBlank()) {
            // MODIFIED: Call the helper to get coordinates from the location name
            startCoordsMono = getCoordinatesFromLocationName(startLocationName)
                    .switchIfEmpty(Mono.error(new RuntimeException("Could not find coordinates for start location: " + startLocationName)));
        } else if (request.currentLocation() != null) {
            startCoordsMono = Mono.just(request.currentLocation());
        } else {
            return Mono.just(new ErrorResponseDTO(
                    "Start location missing. Please provide a 'from <place>' or ensure your current location is supplied."));
        }

        // MODIFIED: Call the helper to get coordinates from the location name
        Mono<Coordinates> endCoordsMono = getCoordinatesFromLocationName(endLocationName)
                .switchIfEmpty(Mono.error(new RuntimeException("Could not find coordinates for destination: " + endLocationName)));

        return Mono.zip(startCoordsMono, endCoordsMono)
                .flatMap(tuple -> {
                    Coordinates start = tuple.getT1();
                    Coordinates end = tuple.getT2();

                    if (!validCoords(start) || !validCoords(end)) {
                        log.warn("Invalid coordinates resolved: start={} end={}", start, end);
                        return Mono.just(new ErrorResponseDTO("Could not resolve one or both locations to valid coordinates."));
                    }

                    String startCoords = start.toString();
                    String endCoords = end.toString();

                    return oneMapService.getBusRoutes(startCoords, endCoords, null)
                            .map(directionsDto -> (BotResponseDTO) directionsDto)
                            .onErrorResume(e -> {
                                log.error("Error fetching directions", e);
                                return Mono.just(new ErrorResponseDTO("Failed to fetch directions."));
                            });
                })
                .onErrorResume(e -> {
                    log.warn("Error during route processing: {}", e.getMessage());
                    return Mono.just(new ErrorResponseDTO("Sorry, I could not process your request: " + e.getMessage()));
                });
    }

    /**
     * Helper method to call the GeocodingService and extract the first valid coordinate pair.
     */
    private Mono<Coordinates> getCoordinatesFromLocationName(String locationName) {
        return geocodingService.getCandidates(locationName)
                .flatMap(candidates -> {
                    if (candidates == null || candidates.isEmpty()) {
                        return Mono.empty(); // No results found
                    }
                    // Attempt to use the first candidate, which is the most likely match
                    GeocodeController.GeocodeCandidate firstCandidate = candidates.get(0);
                    try {
                        String lat = firstCandidate.latitude();
                        String lon = firstCandidate.longitude();
                        return Mono.just(new Coordinates(lat, lon));
                    } catch (NumberFormatException | NullPointerException e) {
                        log.warn("Could not parse coordinates for candidate: {}", firstCandidate, e);
                        return Mono.empty(); // Result was malformed
                    }
                });
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean validCoords(Coordinates c) {
        return c != null && c.latitude() != null && c.longitude() != null;
    }
}