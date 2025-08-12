package com.example.springbackend.repository;

import com.example.springbackend.model.Feedback;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface FeedbackRepository extends ReactiveCrudRepository<Feedback, String> {
    Flux<Feedback> findByUserId(String userId);
    Flux<Feedback> findByUserName(String userName);
}

