package com.example.springbackend.controller;

import com.example.springbackend.model.FavoriteLocation;
import com.example.springbackend.repository.FavoriteLocationRepository;
import com.example.springbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LocationController {

    private final FavoriteLocationRepository favoriteLocationRepository;
    private final UserRepository userRepository;

    /**
     * Get all locations for the authenticated user
     */
    @GetMapping
    public Flux<FavoriteLocation> getUserLocations(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono) {
        return userDetailsMono
                .flatMap(userDetails -> 
                    userRepository.findByUserName(userDetails.getUsername())
                            .map(user -> user.getId())
                )
                .flatMapMany(userId -> 
                    favoriteLocationRepository.findByUserId(userId)
                );
    }

    /**
     * Add a new location for the authenticated user
     */
    @PostMapping
    public Mono<ResponseEntity<FavoriteLocation>> addLocation(
            @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal Mono<UserDetails> userDetailsMono) {
        
        return userDetailsMono
                .flatMap(userDetails -> 
                    userRepository.findByUserName(userDetails.getUsername())
                            .map(user -> user.getId())
                )
                .flatMap(userId -> {
                    FavoriteLocation location = FavoriteLocation.builder()
                            .locationName(request.name())
                            .latitude(request.latitude())
                            .longitude(request.longitude())
                            .userId(userId)
                            .build();
                    
                    return favoriteLocationRepository.save(location);
                })
                .map(savedLocation -> ResponseEntity.status(HttpStatus.CREATED).body(savedLocation))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Update a location for the authenticated user
     */
    @PutMapping("/{locationId}")
    public Mono<ResponseEntity<FavoriteLocation>> updateLocation(
            @PathVariable String locationId,
            @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal Mono<UserDetails> userDetailsMono) {
        
        return userDetailsMono
                .flatMap(userDetails -> 
                    userRepository.findByUserName(userDetails.getUsername())
                            .map(user -> user.getId())
                )
                .flatMap(userId -> 
                    favoriteLocationRepository.findByIdAndUserId(locationId, userId)
                            .flatMap(existingLocation -> {
                                existingLocation.setLocationName(request.name());
                                existingLocation.setLatitude(request.latitude());
                                existingLocation.setLongitude(request.longitude());
                                
                                return favoriteLocationRepository.save(existingLocation);
                            })
                )
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Delete a location for the authenticated user
     */
    @DeleteMapping("/{locationId}")
    public Mono<ResponseEntity<Void>> deleteLocation(
            @PathVariable String locationId,
            @AuthenticationPrincipal Mono<UserDetails> userDetailsMono) {
        
        return userDetailsMono
                .flatMap(userDetails -> 
                    userRepository.findByUserName(userDetails.getUsername())
                            .map(user -> user.getId())
                )
                .flatMap(userId -> 
                    favoriteLocationRepository.findByIdAndUserId(locationId, userId)
                            .flatMap(location -> 
                                favoriteLocationRepository.delete(location)
                                        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                            )
                )
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Get a specific location by ID for the authenticated user
     */
    @GetMapping("/{locationId}")
    public Mono<ResponseEntity<FavoriteLocation>> getLocation(
            @PathVariable String locationId,
            @AuthenticationPrincipal Mono<UserDetails> userDetailsMono) {
        
        return userDetailsMono
                .flatMap(userDetails -> 
                    userRepository.findByUserName(userDetails.getUsername())
                            .map(user -> user.getId())
                )
                .flatMap(userId -> 
                    favoriteLocationRepository.findByIdAndUserId(locationId, userId)
                )
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Request DTO for creating/updating locations
     */
    public record CreateLocationRequest(
            String name,
            Double latitude,
            Double longitude
    ) {}
}