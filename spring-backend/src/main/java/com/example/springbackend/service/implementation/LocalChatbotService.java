package com.example.springbackend.service.implementation;

import com.example.springbackend.controller.GeocodeController; // Required import
import com.example.springbackend.dto.llm.BotResponseDTO;
import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.dto.llm.MessageResponseDTO;
import com.example.springbackend.dto.request.ChatRequest;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.service.ChatbotService;
import com.example.springbackend.service.GeocodingService;
import com.example.springbackend.service.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    private final RoutingService routingService;
    private final GeocodingService geocodingService;

    public LocalChatbotService(RoutingService routingService, GeocodingService geocodingService) {
        this.routingService = routingService;
        this.geocodingService = geocodingService;
    }

    @Override
    public Mono<BotResponseDTO> handleChatInput(ChatRequest request, HttpHeaders incomingHeaders) {
        String input = safeTrim(request.userInput());
        LocalDateTime scheduledTime = TimeParser.parseTomorrowAt(input);
        LocalTime scheduledLocalTime = scheduledTime != null ? scheduledTime.toLocalTime() : null;
        if (input.isEmpty()) {
            return Mono.just(new MessageResponseDTO("Empty input. Please tell me where you want to go, e.g., 'from Bishan to Orchard'."));
        }

        if (GREETING.matcher(input).find()) {
            return Mono.just(new MessageResponseDTO("Hello there! How can I help you today?"));
        }

        String startLocationName = null;
        String endLocationName = null;

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

        if ((endLocationName == null || endLocationName.isBlank()) && !IGNORE_INPUT.matcher(input).find()) {
            String cleanInput = IGNORE_INPUT.matcher(input).replaceAll("").trim();
            if (!cleanInput.isBlank()) {
                endLocationName = cleanInput;
            }
        }

        Mono<ResolvedLocation> startLocationMono = (startLocationName != null && !startLocationName.isBlank())
                ? getResolvedLocation(startLocationName)
                : Mono.justOrEmpty(request.currentLocation())
                .map(coords -> new ResolvedLocation(coords, "Origin"));

        Mono<ResolvedLocation> endLocationMono = (endLocationName != null && !endLocationName.isBlank())
                ? getResolvedLocation(endLocationName)
                : Mono.just(new ResolvedLocation(new Coordinates(0.0, 0.0), "Destination"));

        return Mono.zip(startLocationMono, endLocationMono)
                .flatMap(tuple -> {
                    ResolvedLocation startLoc = tuple.getT1();
                    ResolvedLocation endLoc = tuple.getT2();

                    String startCoordsStr = startLoc.coordinates().toString();
                    String endCoordsStr = endLoc.coordinates().toString();

                    return routingService.getBusRoutes(startCoordsStr, endCoordsStr, null, scheduledLocalTime)
                            .map(routes -> (BotResponseDTO) new DirectionsResponseDTO(
                                    startLoc.displayName(),
                                    endLoc.displayName(),
                                    startLoc.coordinates(),
                                    endLoc.coordinates(),
                                    routes
                            ));
                });
    }

    private Mono<ResolvedLocation> getResolvedLocation(String locationName) {
        return geocodingService.getCandidates(locationName)
                .map(candidates -> {
                    GeocodeController.GeocodeCandidate firstCandidate = candidates.get(0);
                    Coordinates coords = Coordinates.fromString(firstCandidate.latitude() + "," + firstCandidate.longitude());
                    String displayName = firstCandidate.displayName() != null && !firstCandidate.displayName().isBlank()
                            ? firstCandidate.displayName()
                            : locationName;
                    return new ResolvedLocation(coords, displayName);
                });
    }

    private record ResolvedLocation(Coordinates coordinates, String displayName) {}

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
class TimeParser {

    private static final Pattern TOMORROW_AT_PATTERN = Pattern.compile(
            "(?i)\\btomorrow\\s+at\\s+([0-9]{1,2}(:[0-9]{2})?\\s*(am|pm)?)\\b");

    /**
     * Parses a string for "tomorrow at <time>" and returns the corresponding LocalDateTime.
     * If pattern not found or time invalid, returns null.
     */
    public static LocalDateTime parseTomorrowAt(String input) {
        Matcher matcher = TOMORROW_AT_PATTERN.matcher(input);
        if (matcher.find()) {
            String timePart = matcher.group(1).toLowerCase().replaceAll("\\s+", "");
            try {
                // Try am/pm format first
                DateTimeFormatter formatter12h = DateTimeFormatter.ofPattern("h[:mm]a");
                LocalTime time;
                if (timePart.endsWith("am") || timePart.endsWith("pm")) {
                    time = LocalTime.parse(timePart, formatter12h);
                } else {
                    // 24-hour format fallback
                    DateTimeFormatter formatter24h = DateTimeFormatter.ofPattern("H[:mm]");
                    time = LocalTime.parse(timePart, formatter24h);
                }

                // Calculate tomorrow's date + time
                return LocalDateTime.now()
                        .plusDays(1)
                        .withHour(time.getHour())
                        .withMinute(time.getMinute())
                        .withSecond(0)
                        .withNano(0);

            } catch (DateTimeParseException e) {
                // Invalid time format
                return null;
            }
        }
        return null;
    }
}
