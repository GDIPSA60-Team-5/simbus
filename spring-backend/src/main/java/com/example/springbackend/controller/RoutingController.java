package com.example.springbackend.controller;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.dto.llm.RoutingIntentDTO;
import com.example.springbackend.service.OneMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/routing")
public class RoutingController {

    private final OneMapService routingService;

    @Autowired
    public RoutingController(OneMapService routingService) {
        this.routingService = routingService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<DirectionsResponseDTO> getDirections(@RequestBody RoutingIntentDTO intent) {
        return routingService.getBusRoutes(
                intent.start(),
                intent.end(),
                intent.arrivalTime()
        );
    }
}
