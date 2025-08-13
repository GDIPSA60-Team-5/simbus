package com.example.springbackend.repository;

import com.example.springbackend.model.BotLog;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BotLogRepository extends ReactiveCrudRepository<BotLog, String> {

    // Count all bot logs
    @Query(value = "{}", count = true)
    Mono<Long> countRequest();

    // Count successful bot logs
    @Query(value = "{ 'success': true }", count = true)
    Mono<Long> countSuccessfulResponses();
}
