package com.example.springbackend.controller;

import com.example.springbackend.model.FavoriteLocation;
import com.example.springbackend.service.FavoriteLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/favorite-locations")
@RequiredArgsConstructor
public class FavoriteLocationController {

    private final FavoriteLocationService favoriteLocationService;

    // Get all favorite locations for a user
    @GetMapping("/user/{userId}")
    public Flux<FavoriteLocation> getLocationsByUserId(@PathVariable String userId) {
        return favoriteLocationService.getLocationsByUserId(userId);
    }

    // Get favorite location by id
    @GetMapping("/{id}")
    public Mono<FavoriteLocation> getLocationById(@PathVariable String id) {
        return favoriteLocationService.getLocationById(id);
    }

    // Create new favorite location
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FavoriteLocation> createLocation(@RequestBody FavoriteLocation location) {
        return favoriteLocationService.createLocation(location);
    }

    // Update existing favorite location
    @PutMapping("/{id}")
    public Mono<FavoriteLocation> updateLocation(@PathVariable String id, @RequestBody FavoriteLocation updatedLocation) {
        return favoriteLocationService.updateLocation(id, updatedLocation);
    }

    // Delete favorite location by id
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteLocation(@PathVariable String id) {
        return favoriteLocationService.deleteLocation(id);
    }
}
