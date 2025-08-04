package com.example.springbackend.repository;

import com.example.springbackend.model.CommutePlan;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CommutePlanRepository extends ReactiveCrudRepository<CommutePlan, String> {
}
