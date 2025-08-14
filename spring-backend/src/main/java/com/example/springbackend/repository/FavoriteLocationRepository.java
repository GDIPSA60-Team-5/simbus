package com.example.springbackend.repository;

import com.example.springbackend.model.FavoriteLocation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FavoriteLocationRepository extends ReactiveCrudRepository<FavoriteLocation, String> {
    Flux<FavoriteLocation> findByUserId(String userID);
    Mono<FavoriteLocation> findByIdAndUserId(String id, String userId);
}
