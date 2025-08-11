package com.example.springbackend.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.springbackend.model.RouteMongo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RouteMongoRepository extends ReactiveMongoRepository<RouteMongo, String> {
    Flux<RouteMongo> findByDeviceId(String deviceId);
    Mono<RouteMongo> findByDeviceIdAndId(String deviceId, String id);
    Mono<Void> deleteByDeviceIdAndId(String deviceId, String id);
}
