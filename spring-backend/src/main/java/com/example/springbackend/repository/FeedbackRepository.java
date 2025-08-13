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

    @Query("SELECT COUNT(*) FROM feedback")
    Mono<Long> countFeedback();

    @Query("SELECT COUNT(*) FROM feedback WHERE created_at >= $1")
    Mono<Long> countFeedbackSince(LocalDateTime since);
}

