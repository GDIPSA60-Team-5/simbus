package com.example.springbackend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    public void sendNotification(String fcmToken, String title, String body) {
        Message message = Message.builder()
            .setToken(fcmToken)
            .putData("title", title)
            .putData("body", body)
            .build();

        // Log details explicitly
        System.out.println("Sending FCM message with token: " + fcmToken);
        System.out.println("Title: " + title);
        System.out.println("Body: " + body);

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Sent message: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
            // Handle failure, retry, logging, etc.
    }
}
