package com.example.springbackend.controller;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.model.Trip;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TripController {
    
    private final TripService tripService;
    
    @PostMapping("/start")
    public Mono<ResponseEntity<Trip>> startTrip(@RequestBody StartTripRequest request) {
        return tripService.startTrip(
                request.username(),
                request.startLocation(),
                request.endLocation(),
                request.startCoordinates(),
                request.endCoordinates(),
                request.route()
        )
        .map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.badRequest().build())
        .doOnError(e -> log.error("Error starting trip", e));
    }
    
    @PutMapping("/{tripId}/complete")
    public Mono<ResponseEntity<Trip>> completeTrip(@PathVariable String tripId) {
        return tripService.completeTrip(tripId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build())
                .doOnError(e -> log.error("Error completing trip", e));
    }
    
    @PutMapping("/{tripId}/progress")
    public Mono<ResponseEntity<Trip>> updateTripProgress(
            @PathVariable String tripId,
            @RequestBody UpdateProgressRequest request) {
        return tripService.updateTripProgress(tripId, request.currentLegIndex())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build())
                .doOnError(e -> log.error("Error updating trip progress", e));
    }
    
    @GetMapping("/active/{username}")
    public Mono<ResponseEntity<Trip>> getActiveTrip(@PathVariable String username) {
        return tripService.getActiveTrip(username)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
    
    @GetMapping("/history/{username}")
    public Mono<ResponseEntity<List<Trip>>> getTripHistory(@PathVariable String username) {
        return tripService.getTripHistory(username)
                .collectList()
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/{tripId}")
    public Mono<ResponseEntity<Trip>> getTripById(@PathVariable String tripId) {
        return tripService.getTripById(tripId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build())
                .doOnError(e -> log.error("Error getting trip", e));
    }
    
    public record StartTripRequest(
            String username,
            String startLocation,
            String endLocation,
            Coordinates startCoordinates,
            Coordinates endCoordinates,
            DirectionsResponseDTO.RouteDTO route
    ) {}
    
    public record UpdateProgressRequest(
            int currentLegIndex
    ) {}
}