package com.example.springbackend.repository;

import com.example.springbackend.model.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface UserRepository extends ReactiveCrudRepository<User, String> {

    Mono<User> findByUserName(String userName);

    Flux<User> findAllByOrderByCreatedAtDesc();

    // Count all users
    @Query(value = "{}", count = true)
    Mono<Long> countUsers();

    // Count users created since a given time
    @Query(value = "{ 'createdAt': { $gte: ?0 } }", count = true)
    Mono<Long> countUsersSince(LocalDateTime since);

    Mono<Object> findByEmail(String email);
}
