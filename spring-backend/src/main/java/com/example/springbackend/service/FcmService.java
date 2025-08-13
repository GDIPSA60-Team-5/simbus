package com.example.springbackend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    public void sendNotification(String fcmToken, String title, String body) {
        Message message = Message.builder()
            .setToken(fcmToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .build();

        // Log details explicitly
        System.out.println("Sending FCM message with token: " + fcmToken);
        System.out.println("Title: " + title);
        System.out.println("Body: " + body);

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle failure, retry, logging, etc.
        }
    }
}
