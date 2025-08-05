package com.example.springbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("preferred_routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferredRoute {

	@Id
	private String id;

	private String commutePlanId;  // reference to CommutePlan.id

	private String routeId;        // reference to Route.id
}
