package com.example.springbackend.controller;

import com.example.springbackend.model.NotificationJobMongo;
import com.example.springbackend.repository.NotificationJobMongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationMongoController {

    private final NotificationJobMongoRepository notificationRepository;

    public NotificationMongoController(NotificationJobMongoRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Update status (e.g., SKIP or PENDING)
    @PutMapping("/{notificationId}/status")
    public Mono<ResponseEntity<NotificationJobMongo>> updateStatus(
            @PathVariable Integer notificationId,
            @RequestBody Map<String, String> body) {

        String newStatus = body.get("status");
        if (newStatus == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return notificationRepository.findByNotificationId(notificationId)
                .flatMap(job -> {
                    job.setStatus(newStatus);
                    job.setUpdatedAt(LocalDateTime.now());
                    return notificationRepository.save(job);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

