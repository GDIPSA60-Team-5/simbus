package com.example.springbackend.service;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.model.CommutePlan;
import com.example.springbackend.model.Trip;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.model.SavedTripRoute;
import com.example.springbackend.repository.TripRepository;
import com.example.springbackend.repository.SavedTripRouteRepository;
import com.example.springbackend.repository.FavoriteLocationRepository;
import com.example.springbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {
    
    private final TripRepository tripRepository;
    private final TripNotificationService tripNotificationService;
    private final FCMNotificationService fcmNotificationService;
    private final SavedTripRouteRepository savedTripRouteRepository;
    private final FavoriteLocationRepository favoriteLocationRepository;
    private final UserRepository userRepository;
    
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
                            .publishOn(Schedulers.boundedElastic())
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
     * Start trip from commute plan (called by scheduler) - actually creates and starts a trip
     */
    public void startTripFromPlan(CommutePlan plan) {
        try {
            log.info("Starting trip from commute plan: {} for user: {}", plan.getCommutePlanName(), plan.getUserId());
            
            // First get the username from userId, then check for active trips
            getUserName(plan.getUserId())
                    .flatMap(username -> 
                            tripRepository.findByUsernameAndStatus(username, Trip.TripStatus.ON_TRIP)
                                    .hasElement()
                                    .flatMap(hasActiveTrip -> {
                                        if (hasActiveTrip) {
                                            log.warn("User {} (id: {}) already has an active trip, skipping commute plan {}", 
                                                    username, plan.getUserId(), plan.getId());
                                            return Mono.empty();
                                        }
                        
                                        // Get the saved route for this commute plan
                                        if (plan.getSavedTripRouteId() == null) {
                                            log.warn("Commute plan {} has no saved route ID", plan.getId());
                                            return Mono.empty();
                                        }
                        
                                        return savedTripRouteRepository.findById(plan.getSavedTripRouteId())
                                                .flatMap(savedRoute -> createTripFromSavedRoute(plan, savedRoute));
                                    })
                    )
                    .doOnSuccess(trip -> {
                        if (trip != null) {
                            log.info("Successfully created trip {} for commute plan {}", 
                                    trip.getId(), plan.getCommutePlanName());
                            // Send FCM notification for commute started
                            fcmNotificationService.sendCommuteStarted(plan.getUserId(), plan.getCommutePlanName());
                            // Send additional trip notifications (instructions, bus arrivals, etc.)
                            tripNotificationService.sendTripStartNotifications(trip)
                                    .subscribe(
                                            null, // onNext (Void)
                                            error -> log.error("Failed to send trip notifications for trip {}: {}", 
                                                    trip.getId(), error.getMessage())
                                    );
                        }
                    })
                    .doOnError(error -> {
                        log.error("Failed to create trip from commute plan {}: {}", 
                                plan.getId(), error.getMessage());
                    })
                    .subscribe(); // Subscribe to execute the chain
                    
        } catch (Exception e) {
            log.error("Failed to start trip from plan {}: {}", plan.getId(), e.getMessage());
        }
    }
    
    private Mono<Trip> createTripFromSavedRoute(CommutePlan plan, SavedTripRoute savedRoute) {
        // Get location names and username
        return Mono.zip(
                getLocationName(plan.getStartLocationId()),
                getLocationName(plan.getEndLocationId()),
                getUserName(plan.getUserId())
        ).flatMap(tuple -> {
            String startLocationName = tuple.getT1();
            String endLocationName = tuple.getT2();
            String username = tuple.getT3();
            
            // Create the trip
            Trip trip = Trip.builder()
                    .username(username)
                    .startLocation(startLocationName)
                    .endLocation(endLocationName)
                    .startCoordinates(null) // Will be set when user starts navigation
                    .endCoordinates(null)   // Will be set when user starts navigation
                    .route(savedRoute.getRouteData())
                    .status(Trip.TripStatus.ON_TRIP)
                    .currentLegIndex(0)
                    .startTime(LocalDateTime.now())
                    .build();
            
            return tripRepository.save(trip)
                    .doOnSuccess(savedTrip -> {
                        log.info("Created trip {} from commute plan {} - Start: {}, End: {}, User: {}", 
                                savedTrip.getId(), plan.getCommutePlanName(), 
                                startLocationName, endLocationName, username);
                        // Note: Trip notifications are sent from the main startTripFromPlan flow
                    });
        });
    }
    
    private Mono<String> getLocationName(String locationId) {
        if (locationId == null) {
            return Mono.just("Unknown Location");
        }
        
        return favoriteLocationRepository.findById(locationId)
                .map(location -> location.getLocationName())
                .switchIfEmpty(Mono.just("Unknown Location"))
                .onErrorReturn("Unknown Location");
    }
    
    private Mono<String> getUserName(String userId) {
        if (userId == null) {
            return Mono.just("Unknown User");
        }
        
        return userRepository.findById(userId)
                .map(user -> user.getUserName())
                .switchIfEmpty(Mono.just("Unknown User"))
                .onErrorReturn("Unknown User");
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