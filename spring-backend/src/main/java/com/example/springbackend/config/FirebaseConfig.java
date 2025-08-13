package com.example.springbackend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;


@Configuration
public class FirebaseConfig {

    @Value("${firebase.service.account.path}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        try (InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream(serviceAccountPath)) {
            if (serviceAccount == null) {
                throw new RuntimeException("Firebase service account file not found: " + serviceAccountPath);
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped("https://www.googleapis.com/auth/firebase.messaging");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
