package com.example.springbackend.controller;


import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
//import com.example.springbackend.model.RouteMongo;
import com.example.springbackend.model.SavedLocationMongo;
//import com.example.springbackend.repository.RouteMongoRepository;
import com.example.springbackend.repository.SavedLocationMongoRepository;
import com.example.springbackend.service.implementation.BusService;
import com.example.springbackend.service.implementation.NusService;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/bus") // All endpoints in this controller will start with /api/bus
public class BusController {

    private final BusService busService;
    private final NusService nusService;
    private final SavedLocationMongoRepository savedLocationRepository;


    // Spring injects the single BusService bean here
    public BusController(BusService busService, NusService nusService, 
            SavedLocationMongoRepository savedLocationRepository) {
			this.busService = busService;
			this.nusService = nusService;
			this.savedLocationRepository = savedLocationRepository;
}

    @GetMapping("/arrivals")
    public Flux<BusArrival> getArrivalsForStopAndService(
            @RequestParam String busStopQuery,
            @RequestParam(required = false) String serviceNo) {

        return busService.searchBusStops(busStopQuery)
                .next()
                .flatMapMany(busService::getArrivalsForStop)
                .filter(arrival -> serviceNo == null || serviceNo.isBlank() ||
                        arrival.serviceName().equalsIgnoreCase(serviceNo));
    }


    /**
     * Searches for bus stops across all available providers (LTA and NUS).
     *
     * Example URL: GET /api/bus/stops/search?query=UTown
     *
     * @param query The search text from the user.
     * @return A Flux stream of matching BusStop objects.
     */
    @GetMapping("/stops/search")
    public Flux<BusStop> searchBusStops(@RequestParam String query) {
        return busService.searchBusStops(query);
    }

    /**
     * Gets the arrival times for a specific bus stop.
     * The client must send a full BusStop JSON object in the request body.
     * This object is obtained from the search endpoint above.
     *
     * Example URL: POST /api/bus/arrivals
     * Example Body: { "code": "UTOWN", "name": "University Town", ..., "sourceApi": "NUS" }
     *
     * @param busStop The BusStop object selected by the user.
     * @return A Flux stream of BusArrival objects for that stop.
     */
    @PostMapping("/arrivals")
    public Flux<BusArrival> getBusArrivalsForStop(@RequestBody BusStop busStop) {
        return busService.getArrivalsForStop(busStop);
    }
    
    
    
    @GetMapping("/legacy/busServices")
    public Flux<Object> getBusServices(@RequestParam String busStopCode) {
        return busService.searchBusStops(busStopCode)
            .next()
            .flatMapMany(busStop -> {
                if ("LTA".equals(busStop.sourceApi())) {
                   
                    return busService.getArrivalsForStop(busStop)
                        .map(arrival -> Map.of("serviceNo", arrival.serviceName()));
                } else if ("NUS".equals(busStop.sourceApi())) {
                    
                    return nusService.getServiceNamesForStop(busStop.code())
                        .map(serviceName -> Map.of("name", serviceName));
                } else {
                    return Flux.empty();
                }
            })
            .cast(Object.class);
    }


    @GetMapping("/legacy/sgbus")
    public Mono<Map<String, Object>> getSgBusArrival(
            @RequestParam String busStopCode, 
            @RequestParam String busNumber) {
        
        return busService.searchBusStops(busStopCode)
            .filter(stop -> stop.code().equals(busStopCode) && "LTA".equals(stop.sourceApi()))
            .next()
            .flatMapMany(busService::getArrivalsForStop)
            .filter(arrival -> arrival.serviceName().equals(busNumber))
            .next()
            .map(arrival -> {
                List<String> estimatedArrivals = arrival.arrivals().stream()
                    .map(time -> time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .collect(Collectors.toList());
                
                return Map.of(
                    "serviceNo", arrival.serviceName(),
                    "estimatedArrivals", estimatedArrivals
                );
            });
            
    }


    @GetMapping("/legacy/nusbus")
    public Mono<Map<String, Object>> getNusBusArrival(
            @RequestParam String busStopName, 
            @RequestParam String serviceName) {
        
        return busService.searchBusStops(busStopName)
            .filter(stop -> stop.code().equals(busStopName) && "NUS".equals(stop.sourceApi()))
            .next()
            .flatMapMany(busService::getArrivalsForStop)
            .filter(arrival -> arrival.serviceName().equals(serviceName))
            .next()
            .map(arrival -> {
                List<ZonedDateTime> arrivals = arrival.arrivals();
                String arrivalTime = arrivals.isEmpty() ? "" : 
                    arrivals.get(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String nextArrivalTime = arrivals.size() < 2 ? "" : 
                    arrivals.get(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                
                return Map.of(
                    "serviceName", arrival.serviceName(),
                    "arrivalTime", arrivalTime,
                    "nextArrivalTime", nextArrivalTime
                );
            });
            
    }


}