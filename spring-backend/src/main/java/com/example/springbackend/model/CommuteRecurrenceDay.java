package com.example.springbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
public class CommuteRecurrenceDay {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Min(1)
	@Max(7)
	private Integer dayOfWeek; // 1 for Monday ... 7 for Sunday
	
    @ManyToOne
    @JoinColumn(name = "commutePlanId")
    private CommutePlan commutePlan;

	public CommuteRecurrenceDay() { }
	
	public CommuteRecurrenceDay(Integer dayOfWeek, CommutePlan commutePlan) {
		this.dayOfWeek = dayOfWeek;
		this.commutePlan = commutePlan;
	}

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Long getId() {
		return id;
	}

	public CommutePlan getCommutePlan() {
		return commutePlan;
	}

	public void setCommutePlan(CommutePlan commutePlan) {
		this.commutePlan = commutePlan;
	}
	
}
