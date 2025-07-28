package com.example.springbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PreferredRoute {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    @ManyToOne
    @JoinColumn(name = "commutePlanId")
    private CommutePlan commutePlan;

    @ManyToOne
    @JoinColumn(name = "routeId")
    private Route route;
	
	public PreferredRoute() { }

	public PreferredRoute(CommutePlan commutePlan, Route route) {
		this.commutePlan = commutePlan;
		this.route = route;
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
