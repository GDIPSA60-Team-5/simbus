package com.example.springbackend.service;

import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.Trip;
import com.example.springbackend.service.implementation.BusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripNotificationService {

    private final NotificationService notificationService;
    private final BusService busService;
    private final FCMNotificationService fcmNotificationService;

    /**
     * Sends notifications when a trip starts
     */
    public Mono<Void> sendTripStartNotification(Trip trip) {
        String title = "Trip Starting";
        String message = String.format("Trip starting from %s to %s",
                trip.getStartLocation(), trip.getEndLocation());

        return Mono.when(
                // Save notification to database
                notificationService.sendNotification(
                        trip.getUsername(),
                        "TRIP_START",
                        title,
                        message,
                        LocalDateTime.now().plusHours(1)
                ).then(),
                // Send FCM push notification (for additional trip notifications beyond commute started)
                Mono.fromRunnable(() -> {
                    // This provides additional trip details beyond the commute notification
                    log.info("Trip start notification sent for trip {} to user {}", 
                            trip.getId(), trip.getUsername());
                })
        );
    }

    /**
     * Sends notification for the first instruction (typically walk to bus stop)
     */
    public Mono<Void> sendFirstInstructionNotification(Trip trip) {
        if (trip.getRoute() == null || trip.getRoute().getLegs().isEmpty()) {
            return Mono.empty();
        }

        Trip.TripLeg firstLeg = trip.getRoute().getLegs().get(0);
        String title = "First Step";
        String message = getInstructionForLeg(firstLeg);

        return Mono.when(
                // Save notification to database
                notificationService.sendNotification(
                        trip.getUsername(),
                        "TRIP_INSTRUCTION",
                        title,
                        message,
                        LocalDateTime.now().plusHours(1)
                ).then(),
                // Log first instruction
                Mono.fromRunnable(() -> {
                    log.info("First instruction notification sent for trip {} to user {}: {}", 
                            trip.getId(), trip.getUsername(), message);
                })
        );
    }

    /**
     * Sends bus arrival timing notification for the next bus leg
     */
    public Mono<Void> sendBusArrivalNotification(Trip trip) {
        return getBusServiceNumberForNextLeg(trip)
                .flatMap(busInfo -> {
                    if (busInfo.busStopName() != null && busInfo.serviceNumber() != null) {
                        return getBusArrivalTimes(busInfo.busStopName(), busInfo.serviceNumber())
                                .flatMap(arrivalTimes -> {
                                    if (!arrivalTimes.isEmpty()) {
                                        String nextArrival = formatArrivalTime(arrivalTimes.get(0));
                                        String title = "Bus Arrival";
                                        String message = String.format("Bus %s arriving at %s in %s",
                                                busInfo.serviceNumber(), busInfo.busStopName(), nextArrival);

                                        return notificationService.sendNotification(
                                                trip.getUsername(),
                                                "BUS_ARRIVAL",
                                                title,
                                                message,
                                                LocalDateTime.now().plusHours(1)
                                        ).then();
                                    }
                                    return Mono.empty();
                                });
                    }
                    return Mono.empty();
                })
                .onErrorResume(error -> {
                    log.warn("Failed to send bus arrival notification for trip {}: {}",
                            trip.getId(), error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Sends all notifications when a trip starts
     */
    public Mono<Void> sendTripStartNotifications(Trip trip) {
        return Mono.when(
                sendTripStartNotification(trip),
                sendFirstInstructionNotification(trip),
                sendBusArrivalNotification(trip)
        ).onErrorResume(error -> {
            log.error("Error sending trip start notifications for trip {}: {}",
                    trip.getId(), error.getMessage());
            return Mono.empty();
        });
    }

    /**
     * Gets the bus service number for the next bus leg in the trip
     */
    private Mono<BusInfo> getBusServiceNumberForNextLeg(Trip trip) {
        if (trip.getRoute() == null || trip.getRoute().getLegs() == null) {
            return Mono.empty();
        }

        List<Trip.TripLeg> legs = trip.getRoute().getLegs();
        int currentLegIndex = trip.getCurrentLegIndex();

        // Look for the next bus leg starting from current position
        for (int i = currentLegIndex; i < legs.size(); i++) {
            Trip.TripLeg leg = legs.get(i);
            if ("BUS".equalsIgnoreCase(leg.getType())) {
                return Mono.just(new BusInfo(leg.getFromStopName(), leg.getBusServiceNumber()));
            }

            // If current leg is WALK, check if it leads to a bus stop
            if ("WALK".equalsIgnoreCase(leg.getType()) && i + 1 < legs.size()) {
                Trip.TripLeg nextLeg = legs.get(i + 1);
                if ("BUS".equalsIgnoreCase(nextLeg.getType())) {
                    return Mono.just(new BusInfo(leg.getToStopName(), nextLeg.getBusServiceNumber()));
                }
            }
        }

        return Mono.empty();
    }

    /**
     * Gets bus arrival times using the existing bus service
     */
    private Mono<List<ZonedDateTime>> getBusArrivalTimes(String busStopName, String serviceNumber) {
        if (busStopName == null || serviceNumber == null) {
            return Mono.empty();
        }

        return busService.searchBusStops(busStopName)
                .next() // Get first matching bus stop
                .flatMapMany(busService::getArrivalsForStop)
                .filter(arrival -> serviceNumber.equalsIgnoreCase(arrival.serviceName()))
                .next() // Get first matching service
                .map(BusArrival::arrivals)
                .onErrorReturn(List.of()); // Return empty list on error
    }

    /**
     * Formats arrival time for display
     */
    private String formatArrivalTime(ZonedDateTime arrivalTime) {
        ZonedDateTime now = ZonedDateTime.now();
        long minutesUntil = ChronoUnit.MINUTES.between(now, arrivalTime);

        if (minutesUntil <= 0) {
            return "now";
        } else if (minutesUntil == 1) {
            return "1 min";
        } else {
            return minutesUntil + " min";
        }
    }

    /**
     * Gets instruction text for a route leg
     */
    private String getInstructionForLeg(Trip.TripLeg leg) {
        return switch (leg.getType().toUpperCase()) {
            case "WALK" -> String.format("Walk to %s", leg.getToStopName() != null ? leg.getToStopName() : "destination");
            case "BUS" -> String.format("Take Bus %s to %s",
                    leg.getBusServiceNumber() != null ? leg.getBusServiceNumber() : "N/A",
                    leg.getToStopName() != null ? leg.getToStopName() : "destination");
            default -> leg.getInstruction() != null ? leg.getInstruction() : "Continue your journey";
        };
    }

    /**
     * Helper record to store bus information
     */
    private record BusInfo(String busStopName, String serviceNumber) {}
}