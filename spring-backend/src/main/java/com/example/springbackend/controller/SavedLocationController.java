package com.example.springbackend.controller;

import com.example.springbackend.model.SavedLocationMongo;
import com.example.springbackend.repository.SavedLocationMongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api") // All endpoints in this controller will start with /api
public class SavedLocationController {
    private final SavedLocationMongoRepository savedLocationRepository;


    // Spring injects the single BusService bean here
    public SavedLocationController(
                         SavedLocationMongoRepository savedLocationRepository) {
        this.savedLocationRepository = savedLocationRepository;
    }

    @PostMapping("/sync/location")
    public Mono<SavedLocationMongo> syncLocation(
            Authentication authentication, // Change this line
            @RequestBody Map<String, String> locationData) {

        String userId = authentication.getName(); // Change this line

        SavedLocationMongo location = new SavedLocationMongo(
                userId,
                locationData.get("name"),
                locationData.get("postalCode")
        );

        return savedLocationRepository.save(location);
    }

    @GetMapping("/locations")
    public Flux<SavedLocationMongo> getSavedLocations(Authentication authentication) { // Change this
        String userId = authentication.getName(); // Change this
        return savedLocationRepository.findByUserId(userId);
    }

    @DeleteMapping("/locations/{locationId}")
    public Mono<ResponseEntity<Void>> deleteLocation(
            Authentication authentication, // Change this
            @PathVariable String locationId) {

        String userId = authentication.getName(); // Change this
        return savedLocationRepository.findByUserIdAndId(userId, locationId)
                .flatMap(found -> savedLocationRepository.delete(found)
                        .thenReturn(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}