package com.example.springbackend.repository;

import com.example.springbackend.model.CommutePlan;
import org.reactivestreams.Publisher;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CommutePlanRepository extends ReactiveCrudRepository<CommutePlan, String> {
    Flux<CommutePlan> findByUserId(String userId);

    Flux<CommutePlan> findByCommuteRecurrenceDayIdsContaining(String dayCode);
}
