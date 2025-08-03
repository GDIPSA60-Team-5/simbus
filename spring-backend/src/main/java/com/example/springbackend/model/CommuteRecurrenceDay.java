package com.example.springbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

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
public class CommuteRecurrenceDay {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Min(1)
	@Max(7)
	private Integer dayOfWeek; // 1 for Monday ... 7 for Sunday

	@ManyToOne
	@JoinColumn(name = "commutePlanId")
	@ToString.Exclude          // avoid recursion if CommutePlan references back
	@EqualsAndHashCode.Exclude // typically don’t include associations in equals/hashCode
	private CommutePlan commutePlan;
}
