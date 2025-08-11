package com.example.springbackend.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.springbackend.model.SavedLocationMongo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SavedLocationMongoRepository extends ReactiveMongoRepository<SavedLocationMongo, String> {
    Flux<SavedLocationMongo> findByDeviceId(String deviceId);
    Mono<Boolean> existsByDeviceIdAndNameIgnoreCase(String deviceId, String name);
    Mono<Void> deleteByDeviceIdAndId(String deviceId, String id);
}