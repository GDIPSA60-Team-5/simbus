package com.example.springbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSegment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer segmentOrder;
	private String transportMode;
	private Integer estimatedTimeMin;

	@ManyToOne
	@JoinColumn(name = "routeId")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Route route;

	@ManyToOne
	@JoinColumn(name = "fromLocationId")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Location fromLocation;

	@ManyToOne
	@JoinColumn(name = "toLocationId")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Location toLocation;
}
