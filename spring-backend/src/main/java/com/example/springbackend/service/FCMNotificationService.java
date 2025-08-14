package com.example.springbackend.service;

import com.example.springbackend.repository.UserRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMNotificationService {

    private final UserRepository userRepository;

    /**
     * Send commute started notification
     */
    public void sendCommuteStarted(String username, String commutePlanName) {
        userRepository.findByUserName("user")
                .subscribe(user -> {
                    if (user != null && user.getFcmToken() != null) {
                        try {
                            Map<String, String> data = new HashMap<>();
                            data.put("type", "COMMUTE_STARTED");
                            data.put("commutePlanName", commutePlanName);
                            
                            Message message = Message.builder()
                                    .setToken(user.getFcmToken())
                                    .setNotification(Notification.builder()
                                            .setTitle("🚌 Commute Started")
                                            .setBody(String.format("Time to start your commute: %s", commutePlanName))
                                            .build())
                                    .putAllData(data)
                                    .build();
                            
                            String response = FirebaseMessaging.getInstance().send(message);
                            log.info("Sent commute notification to {}: {}", username, response);
                            
                        } catch (Exception e) {
                            log.error("Failed to send FCM to {}: {}", username, e.getMessage());
                        }
                    }
                });
    }
}