package com.example.springbackend.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document("commute_recurrence_days")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommuteRecurrenceDay {

	@Id
	private String id;

	@Min(1)
	@Max(7)
	private Integer dayOfWeek; // 1 for Monday ... 7 for Sunday

	private String commutePlanId; // reference to CommutePlan.id
}
