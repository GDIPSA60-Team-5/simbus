package com.example.springbackend.service.implementation;

import com.example.springbackend.model.BusArrival;
import com.example.springbackend.model.BusStop;
import com.example.springbackend.service.BusServiceProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BusService {

    private final Map<String, BusServiceProvider> providerMap;
    private final Flux<BusStop> allBusStopsCache;

    // Spring injects all beans that implement BusServiceProvider into the list
    public BusService(List<BusServiceProvider> providers) {
        // Map provider API name -> provider instance
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        BusServiceProvider::getApiName,
                        Function.identity()
                ));

        // Log available providers
        System.out.println("Registered BusServiceProviders: " + providerMap.keySet());

        // Merge all bus stops Flux from all providers
        List<Flux<BusStop>> providerFluxes = providers.stream()
                .map(provider -> {
                    System.out.println("Fetching bus stops from provider: " + provider.getApiName());
                    return provider.getAllBusStops()
                            .doOnNext(stop -> System.out.printf("[%s] Loaded stop: %s (%s)%n",
                                    provider.getApiName(), stop.name(), stop.code()));
                })
                .toList();

        // Merge all streams into one and cache them for re-use
        this.allBusStopsCache = Flux.merge(providerFluxes)
                .cache(); // without a TTL means forever until process restart
    }


    /**
     * Searches for bus stops across ALL providers.
     * @param query The user's search text.
     */
    public Flux<BusStop> searchBusStops(String query) {
        if (query == null || query.isBlank()) {
            // Return all stops or empty - choose your preference:
            return allBusStopsCache; // or Flux.empty();
        }

        String normalizedQuery = query.trim().toLowerCase();

        return allBusStopsCache
                .filter(stop -> {
                    String code = stop.code() != null ? stop.code().toLowerCase() : "";
                    String name = stop.name() != null ? stop.name().toLowerCase() : "";
                    return code.contains(normalizedQuery) || name.contains(normalizedQuery);
                });
    }

    /**
     * Gets arrivals by delegating to the correct provider based on the BusStop's source.
     * @param busStop The unified bus stop model, which contains the source API.
     */
    public Flux<BusArrival> getArrivalsForStop(BusStop busStop) {
        BusServiceProvider provider = providerMap.get(busStop.sourceApi());
        if (provider == null) {
            return Flux.error(new IllegalArgumentException("No provider found for API: " + busStop.sourceApi()));
        }
        return provider.getBusArrivals(busStop.code());
    }
}