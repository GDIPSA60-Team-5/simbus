package com.example.springbackend.repository;

import com.example.springbackend.model.DeviceTokenMongo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface DeviceTokenMongoRepository extends ReactiveMongoRepository<DeviceTokenMongo, String> {

    Mono<DeviceTokenMongo> findByDeviceId(String deviceId);
}
