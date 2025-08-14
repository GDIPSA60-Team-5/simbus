package com.example.springbackend.repository;

import com.example.springbackend.model.NotificationJobMongo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationJobMongoRepository extends ReactiveMongoRepository<NotificationJobMongo, String> {
    Flux<NotificationJobMongo> findByRouteId(String routeId);
    Mono<NotificationJobMongo> findByNotificationId(Integer notificationId);
}
