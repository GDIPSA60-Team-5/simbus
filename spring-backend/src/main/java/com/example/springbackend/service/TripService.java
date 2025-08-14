package com.example.springbackend.service;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.model.Trip;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {
    
    private final TripRepository tripRepository;
    private final TripNotificationService tripNotificationService;
    private final FCMNotificationService fcmNotificationService;
    
    public Mono<Trip> startTrip(String username, String startLocation, String endLocation, 
                               Coordinates startCoordinates, Coordinates endCoordinates,
                               DirectionsResponseDTO.RouteDTO routeDTO) {
        
        return tripRepository.findByUsernameAndStatus(username, Trip.TripStatus.ON_TRIP)
                .hasElement()
                .flatMap(hasActiveTrip -> {
                    if (hasActiveTrip) {
                        return Mono.error(new RuntimeException("User already has an active trip"));
                    }
                    
                    // Convert RouteDTO to TripRoute
                    Trip.TripRoute tripRoute = convertToTripRoute(routeDTO);
                    
                    Trip trip = Trip.builder()
                            .username(username)
                            .startLocation(startLocation)
                            .endLocation(endLocation)
                            .startCoordinates(startCoordinates)
                            .endCoordinates(endCoordinates)
                            .route(tripRoute)
                            .status(Trip.TripStatus.ON_TRIP)
                            .currentLegIndex(0)
                            .startTime(LocalDateTime.now())
                            .build();
                    
                    return tripRepository.save(trip)
                            .doOnSuccess(savedTrip -> {
                                log.info("Started new trip for username: {}", username);
                                // Send trip start notifications asynchronously
                                tripNotificationService.sendTripStartNotifications(savedTrip)
                                        .subscribe(
                                                null, // onNext (Void)
                                                error -> log.error("Failed to send trip notifications for trip {}: {}", 
                                                        savedTrip.getId(), error.getMessage())
                                        );
                            });
                });
    }
    
    // Backward compatibility method
    public Mono<Trip> startTrip(String username, String startLocation, String endLocation, 
                               DirectionsResponseDTO.RouteDTO routeDTO) {
        return startTrip(username, startLocation, endLocation, null, null, routeDTO);
    }
    
    public Mono<Trip> completeTrip(String tripId) {
        return tripRepository.findById(tripId)
                .switchIfEmpty(Mono.error(new RuntimeException("Trip not found")))
                .flatMap(trip -> {
                    trip.setStatus(Trip.TripStatus.COMPLETED);
                    trip.setEndTime(LocalDateTime.now());
                    
                    return tripRepository.save(trip)
                            .doOnSuccess(savedTrip -> log.info("Completed trip: {}", tripId));
                });
    }
    
    public Mono<Trip> updateTripProgress(String tripId, int currentLegIndex) {
        return tripRepository.findById(tripId)
                .switchIfEmpty(Mono.error(new RuntimeException("Trip not found")))
                .flatMap(trip -> {
                    trip.setCurrentLegIndex(currentLegIndex);
                    
                    return tripRepository.save(trip)
                            .doOnSuccess(savedTrip -> log.info("Updated trip progress - tripId: {}, currentLeg: {}", tripId, currentLegIndex));
                });
    }
    
    public Mono<Trip> getActiveTrip(String username) {
        return tripRepository.findByUsernameAndStatus(username, Trip.TripStatus.ON_TRIP);
    }
    
    public Flux<Trip> getTripHistory(String username) {
        return tripRepository.findByUsernameOrderByStartTimeDesc(username);
    }
    
    public Mono<Trip> getTripById(String tripId) {
        return tripRepository.findById(tripId)
                .switchIfEmpty(Mono.error(new RuntimeException("Trip not found")));
    }
    
    /**
     * Simple method to start trip from commute plan (called by scheduler)
     */
    public void startTripFromPlan(com.example.springbackend.model.CommutePlan plan) {
        try {
            // Send FCM notification
            fcmNotificationService.sendCommuteStarted(plan.getUserId(), plan.getCommutePlanName());
            log.info("Started commute for plan: {} user: {}", plan.getCommutePlanName(), plan.getUserId());
        } catch (Exception e) {
            log.error("Failed to start trip from plan {}: {}", plan.getId(), e.getMessage());
        }
    }
    
    private Trip.TripRoute convertToTripRoute(DirectionsResponseDTO.RouteDTO routeDTO) {
        List<Trip.TripLeg> tripLegs = routeDTO.legs().stream()
                .map(leg -> Trip.TripLeg.builder()
                        .type(leg.type())
                        .durationInMinutes(leg.durationInMinutes())
                        .busServiceNumber(leg.busServiceNumber())
                        .instruction(leg.instruction())
                        .legGeometry(leg.legGeometry())
                        .routePoints(leg.routePoints())
                        .fromStopName(leg.fromStopName())
                        .fromStopCode(leg.fromStopCode())
                        .toStopName(leg.toStopName())
                        .toStopCode(leg.toStopCode())
                        .build())
                .toList();
        
        return Trip.TripRoute.builder()
                .durationInMinutes(routeDTO.durationInMinutes())
                .legs(tripLegs)
                .summary(routeDTO.summary())
                .build();
    }
}