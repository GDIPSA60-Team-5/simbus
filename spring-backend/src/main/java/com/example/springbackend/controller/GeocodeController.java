package com.example.springbackend.controller;

import com.example.springbackend.model.Coordinates;
import com.example.springbackend.service.GeocodingService;
import com.example.springbackend.service.ReverseGeocodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GeocodeController {

    private static final Logger log = LoggerFactory.getLogger(GeocodeController.class);

    private final GeocodingService geocodingService;
    private final ReverseGeocodeService reverseGeocodeService;

    public GeocodeController(GeocodingService geocodingService,
                             ReverseGeocodeService reverseGeocodeService) {
        this.geocodingService = geocodingService;
        this.reverseGeocodeService = reverseGeocodeService;
    }

    /**
     * GET /api/geocode?locationName=Orchard
     * Returns list of candidate locations for a location name.
     */
    @GetMapping(value = "/geocode", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> geocode(
            @RequestParam @NotBlank String locationName) {

        return geocodingService.getCandidates(locationName)
                .map(candidates -> {
                    if (candidates.isEmpty()) {
                        return ResponseEntity.notFound().build();
                    }
                    GeocodeListResponse response = new GeocodeListResponse(
                            candidates.size(), 1, 1, candidates);
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/reverse-geocode?x=...&y=...&buffer=
     * Returns a list of formatted address candidates for given SVY21 x,y coordinates.
     */
    @GetMapping(value = "/reverse-geocode", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> reverseGeocode(
            @RequestParam("x") @NotBlank String x,
            @RequestParam("y") @NotBlank String y
          ) {

        return reverseGeocodeService.getCandidates(x, y)
                .map(candidates -> {
                    if (candidates.isEmpty()) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(new ReverseGeocodeListResponse(candidates));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public record GeocodeCandidate(
            String latitude,
            String longitude,
            String displayName,
            String postalCode,
            String block,
            String road,
            String building) {}

    public record GeocodeListResponse(
            int found,
            int pageNum,
            int totalNumPages,
            List<GeocodeCandidate> results) {}

    public record ReverseGeocodeCandidate(String address) {}

    public record ReverseGeocodeListResponse(List<ReverseGeocodeCandidate> candidates) {}
}
