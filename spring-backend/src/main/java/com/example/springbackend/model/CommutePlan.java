package com.example.springbackend.model;

import java.time.LocalTime;
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
public class CommutePlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String commutePlanName;
	private LocalTime notifyAt;
	private LocalTime arrivalTime;
	private Integer reminderOffsetMin;
	private Boolean recurrence;
	@ManyToOne
	@JoinColumn(name = "start_location_id")
	private Location startLocation;
	@ManyToOne
	@JoinColumn(name = "end_location_id")
	private Location endLocation;

	@ManyToOne
	@JoinColumn(name = "userId")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User user;

	@OneToMany(mappedBy = "commutePlan", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<CommuteHistory> commuteHistory = new ArrayList<>();

	@OneToMany(mappedBy = "commutePlan", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<PreferredRoute> preferredRoutes = new ArrayList<>();

	@OneToMany(mappedBy = "commutePlan", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<CommuteRecurrenceDay> commuteRecurrenceDays = new ArrayList<>();
}
