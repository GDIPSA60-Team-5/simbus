package com.example.springbackend.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Route {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private String routeName;
	private Integer estimatedTimeMin;
    
    @ManyToOne
    @JoinColumn(name = "startLocationId")
    private Location startLocation;

    @ManyToOne
    @JoinColumn(name = "endLocationId")
    private Location endLocation;
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<RouteSegment> routeSegments;
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<CommuteHistory> commuteHistoryList;
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<PreferredRoute> preferredRoutes;
	
	public Route() { }
	
	public Route(String routeName, Integer estimatedTimeMin, Location startLocation, Location endLocation) {
		this.routeName = routeName;
		this.estimatedTimeMin = estimatedTimeMin;
		this.startLocation = startLocation;
		this.endLocation = endLocation;
	}

	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName;
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

	public Location getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(Location startLocation) {
		this.startLocation = startLocation;
	}

	public Location getEndLocation() {
		return endLocation;
	}

	public void setEndLocation(Location endLocation) {
		this.endLocation = endLocation;
	}

	public List<RouteSegment> getRouteSegments() {
		return routeSegments;
	}

	public void setRouteSegments(List<RouteSegment> routeSegments) {
		this.routeSegments = routeSegments;
	}

	public List<CommuteHistory> getCommuteHistory() {
		return commuteHistoryList;
	}

	public void setCommuteHistory(List<CommuteHistory> commuteHistoryList) {
		this.commuteHistoryList = commuteHistoryList;
	}

	public List<CommuteHistory> getCommuteHistoryList() {
		return commuteHistoryList;
	}

	public void setCommuteHistoryList(List<CommuteHistory> commuteHistoryList) {
		this.commuteHistoryList = commuteHistoryList;
	}

	public List<PreferredRoute> getPreferredRoutes() {
		return preferredRoutes;
	}

	public void setPreferredRoutes(List<PreferredRoute> preferredRoutes) {
		this.preferredRoutes = preferredRoutes;
	}
	
}
