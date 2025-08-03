package com.example.springbackend.model;

import java.time.LocalDateTime;

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
public class CommuteHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String status; // consider replacing with an enum if values are bounded
	private LocalDateTime startedAt;
	private LocalDateTime endedAt;

	@ManyToOne
	@JoinColumn(name = "commutePlanId")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private CommutePlan commutePlan;

	@ManyToOne
	@JoinColumn(name = "routeId")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Route route;
}
