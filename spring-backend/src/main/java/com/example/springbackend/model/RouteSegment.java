package com.example.springbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class RouteSegment {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private Integer segmentOrder;
	private String transportMode;
	private Integer estimatedTimeMin;
	
    @ManyToOne
    @JoinColumn(name = "routeId")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "fromLocationId")
    private Location fromLocation;

    @ManyToOne
    @JoinColumn(name = "toLocationId")
    private Location toLocation;
	
	public RouteSegment() { }
	
	public RouteSegment(Integer segmentOrder, String transportMode, Integer estimatedTimeMin) {
		this.segmentOrder = segmentOrder;
		this.transportMode = transportMode;
		this.estimatedTimeMin = estimatedTimeMin;
	}

	public Integer getSegmentOrder() {
		return segmentOrder;
	}

	public void setSegmentOrder(Integer segmentOrder) {
		this.segmentOrder = segmentOrder;
	}

	public String getTransportMode() {
		return transportMode;
	}

	public void setTransportMode(String transportMode) {
		this.transportMode = transportMode;
	}

	public Integer getEstimatedTimeMin() {
		return estimatedTimeMin;
	}

	public void setEstimatedTimeMin(Integer estimatedTimeMin) {
		this.estimatedTimeMin = estimatedTimeMin;
	}

	public Long getId() {
		return id;
	}
	
}
