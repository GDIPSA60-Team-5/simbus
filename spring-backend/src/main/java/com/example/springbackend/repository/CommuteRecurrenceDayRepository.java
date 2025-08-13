package com.example.springbackend.repository;

import com.example.springbackend.model.CommuteRecurrenceDay;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommuteRecurrenceDayRepository
        extends ReactiveCrudRepository<CommuteRecurrenceDay, String> {

    Flux<CommuteRecurrenceDay> findByCommutePlanId(String commutePlanId);
    Mono<Void> deleteByCommutePlanId(String commutePlanId);
}
