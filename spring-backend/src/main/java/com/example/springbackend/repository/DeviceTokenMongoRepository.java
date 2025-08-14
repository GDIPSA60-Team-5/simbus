package com.example.springbackend.repository;

import com.example.springbackend.model.DeviceTokenMongo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface DeviceTokenMongoRepository extends ReactiveMongoRepository<DeviceTokenMongo, String> {

    Flux<DeviceTokenMongo> findByDeviceId(String deviceId);
}
