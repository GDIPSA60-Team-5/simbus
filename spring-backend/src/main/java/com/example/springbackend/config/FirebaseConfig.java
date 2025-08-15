package com.example.springbackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // Try to load from classpath first
            InputStream serviceAccount = null;
            try {
                serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
            } catch (Exception e) {
                log.warn("Firebase service account file not found in classpath, trying file system...");
                // Fallback to file system path if classpath doesn't work
                try {
                    serviceAccount = new FileInputStream("src/main/resources/firebase-service-account.json");
                } catch (Exception ex) {
                    log.error("Firebase service account file not found in file system either. FCM will not work.");
                    return;
                }
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized successfully");
            }

        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }
}