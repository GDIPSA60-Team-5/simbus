package com.example.springbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("saved_trip_routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedTripRoute {
    @Id 
    private String id;
    private Trip.TripRoute routeData;
    private String userId;
}