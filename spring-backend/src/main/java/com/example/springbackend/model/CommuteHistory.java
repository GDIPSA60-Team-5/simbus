package com.example.springbackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class CommuteHistory {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    
    @ManyToOne
    @JoinColumn(name = "commutePlanId")
    private CommutePlan commutePlan;

    @ManyToOne
    @JoinColumn(name = "routeId")
    private Route route;
    
    public CommuteHistory() { }
    
	public CommuteHistory(String status, LocalDateTime startedAt, LocalDateTime endedAt, CommutePlan commutePlan,
			Route route) {
		this.status = status;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
		this.commutePlan = commutePlan;
		this.route = route;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public LocalDateTime getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(LocalDateTime endedAt) {
		this.endedAt = endedAt;
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

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}
    
}
