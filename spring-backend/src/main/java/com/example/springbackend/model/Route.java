package com.example.springbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.util.List;

@Document("routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {

	@Id
	private String id;

	private String routeName;
	private Integer estimatedTimeMin;

	private String startLocationId;  // reference to Location.id
	private String endLocationId;    // reference to Location.id

	// Instead of @OneToMany lists, store only the IDs or load separately
	private List<String> routeSegmentIds;     // IDs of RouteSegment documents
	private List<String> commuteHistoryIds;   // IDs of CommuteHistory documents
	private List<String> preferredRouteIds;   // IDs of PreferredRoute documents
}
