package com.example.springbackend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FcmService {

    public void sendNotification(String fcmToken, Map<String,String> data) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .putAllData(data)  // send the map as FCM data payload
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

}
