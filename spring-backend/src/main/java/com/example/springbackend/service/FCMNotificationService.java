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
     * Send enhanced commute started notification with trip details
     */
    public void sendCommuteStarted(String userId, String commutePlanName) {
        log.info("FCM: Starting to send commute notification - userId: {}, planName: {}", userId, commutePlanName);
        
        userRepository.findById(userId)
                .subscribe(user -> {
                    log.info("FCM: Found user for userId {}: {}", userId, user != null ? user.getUserName() : "null");
                    
                    if (user != null && user.getFcmToken() != null) {
                        log.info("FCM: User has FCM token, sending notification to token: {}...", 
                                user.getFcmToken().substring(0, Math.min(20, user.getFcmToken().length())));
                        try {
                            // Create data payload for enhanced notification
                            Map<String, String> data = new HashMap<>();
                            data.put("type", "COMMUTE_STARTED");
                            data.put("commutePlanName", commutePlanName);
                            data.put("userId", userId);
                            
                            Message message = Message.builder()
                                    .setToken(user.getFcmToken())
                                    .setNotification(Notification.builder()
                                            .setTitle("Commute " + commutePlanName + " Started")
                                            .setBody("Tap to open navigation and start your journey")
                                            .build())
                                    .putAllData(data)
                                    .build();
                            
                            String response = FirebaseMessaging.getInstance().send(message);
                            log.info("FCM: Successfully sent notification to userId {}: {}", userId, response);
                            
                        } catch (Exception e) {
                            log.error("FCM: Failed to send FCM to userId {}: {}", userId, e.getMessage(), e);
                        }
                    } else if (user != null) {
                        log.warn("FCM: User {} has no FCM token", userId);
                    } else {
                        log.warn("FCM: No user found for userId {}", userId);
                    }
                }, error -> {
                    log.error("FCM: Error finding user by userId {}: {}", userId, error.getMessage(), error);
                });
    }
}