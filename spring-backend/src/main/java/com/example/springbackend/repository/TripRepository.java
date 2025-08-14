package com.example.springbackend.repository;

import com.example.springbackend.model.Trip;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TripRepository extends ReactiveCrudRepository<Trip, String> {
    
    Flux<Trip> findByUsername(String username);
    
    Mono<Trip> findByUsernameAndStatus(String username, Trip.TripStatus status);
    
    Flux<Trip> findByUsernameOrderByStartTimeDesc(String username);
}