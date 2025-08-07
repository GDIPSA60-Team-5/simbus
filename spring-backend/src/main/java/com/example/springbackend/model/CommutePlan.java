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

	@Id
	private String id;

	private String commutePlanName;
	private LocalTime notifyAt;
	private LocalTime arrivalTime;
	private Integer reminderOffsetMin;
	private Boolean recurrence;

	private String startLocationId;  // reference to Location.id
	private String endLocationId;    // reference to Location.id
	private String userId;           // reference to User.id

	// IDs for related entities, loaded separately if needed
	private List<String> commuteHistoryIds;
	private List<String> preferredRouteIds;
	private List<String> commuteRecurrenceDayIds;
}
