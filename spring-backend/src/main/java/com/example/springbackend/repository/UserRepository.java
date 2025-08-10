package com.example.springbackend.repository;

import com.example.springbackend.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, String> {
    Mono<User> findByUserName(String userName);
    Flux<User> findAllByOrderByCreatedAtDesc();
}
