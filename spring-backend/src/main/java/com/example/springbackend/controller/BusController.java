package com.example.springbackend.controller;


import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.service.implementation.BusService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/bus") // All endpoints in this controller will start with /api/bus
public class BusController {

    private final BusService busService;

    // Spring injects the single BusService bean here
    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping("/arrivals")
    public Flux<BusArrival> getArrivalsForStopAndService(
            @RequestParam String busStopCode,
            @RequestParam(required = false) String serviceNo) {

        return busService.searchBusStops(busStopCode)
                .filter(stop -> stop.code().equalsIgnoreCase(busStopCode))
                .next() // take first exact match
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
}