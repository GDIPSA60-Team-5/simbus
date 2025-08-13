package com.example.springbackend.controller;

import com.example.springbackend.model.DeviceTokenMongo;
import com.example.springbackend.repository.DeviceTokenMongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeviceTokenController {

    private final DeviceTokenMongoRepository deviceTokenRepository;

    public DeviceTokenController(DeviceTokenMongoRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @PostMapping("/device-token")
    public Mono<ResponseEntity<String>> saveOrUpdateDeviceToken(@RequestBody Map<String, String> payload) {
        String deviceId = payload.get("deviceId");
        String fcmToken = payload.get("fcmToken");

        if (deviceId == null || fcmToken == null) {
            return Mono.just(ResponseEntity.badRequest().body("Missing deviceId or fcmToken"));
        }

        return deviceTokenRepository.findByDeviceId(deviceId)
                .flatMap(existingToken -> {
                    existingToken.setFcmToken(fcmToken);
                    return deviceTokenRepository.save(existingToken);
                })
                .switchIfEmpty(
                        deviceTokenRepository.save(new DeviceTokenMongo(deviceId, fcmToken))
                )
                .map(saved -> ResponseEntity.ok("Device token saved/updated successfully"));
    }
}
