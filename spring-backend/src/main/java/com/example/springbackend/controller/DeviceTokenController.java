package com.example.springbackend.controller;

import com.example.springbackend.model.DeviceTokenMongo;
import com.example.springbackend.repository.DeviceTokenMongoRepository;
import com.example.springbackend.service.FcmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeviceTokenController {

    private final DeviceTokenMongoRepository deviceTokenRepository;
    private final FcmService fcmService;

    public DeviceTokenController(DeviceTokenMongoRepository deviceTokenRepository, FcmService fcmService) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.fcmService = fcmService;
    }

    @PostMapping("/device-token")
    public Mono<ResponseEntity<Map<String, String>>> saveOrUpdateDeviceToken(@RequestBody Map<String, String> payload) {
        String deviceId = payload.get("deviceId");
        String fcmToken = payload.get("fcmToken");

        if (deviceId == null || fcmToken == null) {
            return Mono.just(ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Missing deviceId or fcmToken")));
        }

        return deviceTokenRepository.findByDeviceId(deviceId)
                .flatMap(existingToken -> {
                    existingToken.setFcmToken(fcmToken);
                    return deviceTokenRepository.save(existingToken);
                })
                .switchIfEmpty(
                        deviceTokenRepository.save(new DeviceTokenMongo(deviceId, fcmToken))
                )
                .doOnSuccess(saved -> {
                    // Send a test notification after saving
                    fcmService.sendNotification(fcmToken, Map.of(
                            "title", "Welcome!",
                            "body", "Device token registered successfully."
                    ));
                })
                .map(saved -> ResponseEntity.ok(Map.of("message", "Device token saved/updated and test notification sent")));
    }
}
