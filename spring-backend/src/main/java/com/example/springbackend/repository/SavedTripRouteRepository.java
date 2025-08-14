package com.example.springbackend.repository;

import com.example.springbackend.model.SavedTripRoute;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface SavedTripRouteRepository extends ReactiveMongoRepository<SavedTripRoute, String> {
    Flux<SavedTripRoute> findByUserId(String userId);
}