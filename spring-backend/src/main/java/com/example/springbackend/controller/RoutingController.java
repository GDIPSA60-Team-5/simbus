package com.example.springbackend.controller;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.dto.llm.RoutingIntentDTO;
import com.example.springbackend.model.Coordinates;
import com.example.springbackend.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/routing")
public class RoutingController {

    private final RoutingService routingService;

    @Autowired
    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<DirectionsResponseDTO> getDirections(@RequestBody RoutingIntentDTO intent) {
        return routingService.getBusRoutes(
                        intent.startCoordinates(),
                        intent.endCoordinates(),
                        intent.arrivalTime(),
                        intent.startTime()
                )
                .map(routes -> new DirectionsResponseDTO(
                        intent.startLocation() != null && !intent.startLocation().isBlank()
                                ? intent.startLocation()
                                : "Origin",
                        intent.endLocation() != null && !intent.endLocation().isBlank()
                                ? intent.endLocation()
                                : "Destination",
                        Coordinates.fromString(intent.startCoordinates()),
                        Coordinates.fromString(intent.endCoordinates()),
                        routes
                ));
    }

}
