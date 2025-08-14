package com.example.springbackend.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "trips")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    
    @Id
    private String id;
    
    private String username;
    
    // Store the complete route data for the trip
    private String startLocation;
    private String endLocation;
    private Coordinates startCoordinates;
    private Coordinates endCoordinates;
    private TripRoute route;
    
    // Trip status: ON_TRIP, COMPLETED
    private TripStatus status;
    
    // Track current progress in the route
    private Integer currentLegIndex;
    
    // Timestamps
    @CreatedDate
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum TripStatus {
        ON_TRIP,
        COMPLETED
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripRoute {
        private int durationInMinutes;
        private List<TripLeg> legs;
        private String summary;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripLeg {
        private String type;
        private int durationInMinutes;
        private String busServiceNumber;
        private String instruction;
        private String legGeometry;
        private List<Coordinates> routePoints;
        private String fromStopName;
        private String fromStopCode;
        private String toStopName;
        private String toStopCode;
    }
}