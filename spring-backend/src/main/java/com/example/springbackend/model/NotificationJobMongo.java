package com.example.springbackend.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "notifications")
@Data
public class NotificationJobMongo {
    @Id
    private String id;

    private Integer notificationId;

    private String routeId;         // Reference to RouteMongo
    private String deviceId;        // Target device

    private String scheduledTime; // Exact time to send
    private String timezone;        // For display or debugging

    private List<Boolean> selectedDays;

    private String messageTitle;    // Notification title
    private String messageBody;     // Notification body

    private String status;          // PENDING, SENT, FAILED, CANCELLED

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
