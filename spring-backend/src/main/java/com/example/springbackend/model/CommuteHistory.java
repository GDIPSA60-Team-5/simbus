package com.example.springbackend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document("commute_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommuteHistory {

	@Id
	private String id;

	private String status; // Consider enum conversion in application logic

	private LocalDateTime startedAt;
	private LocalDateTime endedAt;

	private String commutePlanId; // Reference to CommutePlan.id
	private String routeId;       // Reference to Route.id
}
