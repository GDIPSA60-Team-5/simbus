package com.example.springbackend.repository;

import com.example.springbackend.model.UserNotification;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface NotificationRepository extends ReactiveCrudRepository<UserNotification, String> {
    Flux<UserNotification> findByUserId(Long userID);
}
