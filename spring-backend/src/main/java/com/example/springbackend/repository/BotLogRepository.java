package com.example.springbackend.repository;

import com.example.springbackend.model.BotLog;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BotLogRepository extends ReactiveCrudRepository<BotLog, Long> {
    @Query("SELECT COUNT(*) FROM botlog")
    Mono<Long> countRequest();

    @Query("SELECT COUNT(*) FROM botlog WHERE success = true")
    Mono<Long> countSuccessfulResponses();
}
