package com.example.springbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

	@Id
	private String id;

	private String locationName;
	private Double latitude;
	private Double longitude;

	@Indexed // for lookups by userId
	private String userId; // reference to User.id (no automatic relationship)
}
