package com.example.springbackend.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.springbackend.model.SavedLocationMongo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SavedLocationMongoRepository extends ReactiveMongoRepository<SavedLocationMongo, String> {
    Flux<SavedLocationMongo> findByUserId(String userId);
    Mono<SavedLocationMongo> findByUserIdAndId(String userId, String id);
    Mono<Void> deleteByUserIdAndId(String userId, String id);
}