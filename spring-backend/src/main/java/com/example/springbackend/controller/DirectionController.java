package com.example.springbackend.controller;

import com.example.springbackend.dto.llm.DirectionsResponseDTO;
import com.example.springbackend.service.OneMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/directions")
public class DirectionController {

    private final OneMapService directionService;

    @Autowired
    public DirectionController(OneMapService directionService) {
        this.directionService = directionService;
    }

    /**
     * Endpoint to get directions between start and end locations.
     * Example request: GET /api/directions?start=1.3521,103.8198&end=1.3000,103.8000
     *
     * @param start coordinates as "lat,lon"
     * @param end coordinates as "lat,lon"
     * @return Mono of DirectionsResponseDTO
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<DirectionsResponseDTO> getDirections(
            @RequestParam String start,
            @RequestParam String end
    ) {
        return directionService.getBusRoutes(start, end);
    }
}
