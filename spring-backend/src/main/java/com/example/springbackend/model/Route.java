package com.example.springbackend.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

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
public class Route {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String routeName;
	private Integer estimatedTimeMin;

	@ManyToOne
	@JoinColumn(name = "startLocationId")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Location startLocation;

	@ManyToOne
	@JoinColumn(name = "endLocationId")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Location endLocation;

	@OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<RouteSegment> routeSegments = new ArrayList<>();

	@OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<CommuteHistory> commuteHistoryList = new ArrayList<>();

	@OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<PreferredRoute> preferredRoutes = new ArrayList<>();
}
