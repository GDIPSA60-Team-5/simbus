package com.example.springbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("route_segments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSegment {

	@Id
	private String id;

	private Integer segmentOrder;
	private String transportMode;
	private Integer estimatedTimeMin;

	private String routeId;          // reference to Route.id
	private String fromLocationId;   // reference to Location.id
	private String toLocationId;     // reference to Location.id
}
