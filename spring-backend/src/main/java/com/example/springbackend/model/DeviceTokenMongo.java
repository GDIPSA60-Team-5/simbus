package com.example.springbackend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "device_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenMongo {

    @Id
    private String id;

    private String deviceId;

    private String fcmToken;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Optional constructor without id, createdAt, updatedAt
    public DeviceTokenMongo(String deviceId, String fcmToken) {
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
