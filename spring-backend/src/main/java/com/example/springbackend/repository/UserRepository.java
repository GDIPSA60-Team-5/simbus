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
    @Query("SELECT COUNT(*) FROM users")
    Mono<Long> countUsers();

    @Query("SELECT COUNT(*) FROM users WHERE created_at >= $1")
    Mono<Long> countUsersSince(LocalDateTime since);
}
