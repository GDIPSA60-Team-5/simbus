package com.example.springbackend.controller;

import com.example.springbackend.model.SavedTripRoute;
import com.example.springbackend.model.Trip;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.SavedTripRouteRepository;
import com.example.springbackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/saved-trip-routes")
public class SavedTripRouteController {

    private final SavedTripRouteRepository savedTripRouteRepository;
    private final UserRepository userRepository;

    public SavedTripRouteController(SavedTripRouteRepository savedTripRouteRepository,
                                    UserRepository userRepository) {
        this.savedTripRouteRepository = savedTripRouteRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new saved trip route for the authenticated user
     */
    @PostMapping
    public Mono<ResponseEntity<SavedTripRoute>> createSavedTripRoute(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @Valid @RequestBody CreateSavedTripRouteRequest request) {

        return getUserFromPrincipal(principal)
                .flatMap(user -> {
                    SavedTripRoute savedRoute = SavedTripRoute.builder()
                            .routeData(request.routeData())
                            .userId(user.getId())
                            .build();
                    return savedTripRouteRepository.save(savedRoute);
                })
                .map(savedRoute -> ResponseEntity.status(HttpStatus.CREATED).body(savedRoute))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Get a saved trip route by ID (only if it belongs to the authenticated user)
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<SavedTripRoute>> getSavedTripRoute(
            @AuthenticationPrincipal Mono<UserDetails> principal,
            @PathVariable String id) {

        return getUserFromPrincipal(principal)
                .flatMap(user -> savedTripRouteRepository.findById(id)
                        .filter(route -> route.getUserId().equals(user.getId())))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Helper method to extract user from authentication principal
     */
    private Mono<User> getUserFromPrincipal(Mono<UserDetails> principal) {
        return principal
                .flatMap(userDetails -> userRepository.findByUserName(userDetails.getUsername()))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    /**
     * Request record for creating saved trip routes
     */
    public record CreateSavedTripRouteRequest(
            Trip.TripRoute routeData
    ) {}
}