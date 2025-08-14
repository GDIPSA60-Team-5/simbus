package com.example.springbackend.repository;

import com.example.springbackend.model.Feedback;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface FeedbackRepository extends ReactiveCrudRepository<Feedback, String> {

    Flux<Feedback> findByUserId(String userId);
    Flux<Feedback> findByUserName(String userName);

    // Count all feedback
    @Query(value = "{}", count = true)
    Mono<Long> countFeedback();

    // Count feedback since a certain date
    @Query(value = "{ 'createdAt': { $gte: ?0 } }", count = true)
    Mono<Long> countFeedbackSince(LocalDateTime since);
}
