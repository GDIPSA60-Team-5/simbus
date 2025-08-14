package com.example.springbackend.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("commute_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommutePlan {
		@Id private String id;
		private String commutePlanName;
		private LocalTime notifyAt;
		private LocalTime arrivalTime;
		private Integer reminderOffsetMin;
		private Boolean recurrence;
		private String startLocationId;
		private String endLocationId;
		private String userId;
		private String savedTripRouteId;
		private List<String> commuteRecurrenceDayIds;
}
