package com.example.springbackend.dto;

import java.util.List;

public class SgBusArrivalInfoDTO {
	
	private String serviceNo;
	private List<String> estimatedArrivals;
	
	
	public SgBusArrivalInfoDTO() {
	}
	
	public SgBusArrivalInfoDTO(String serviceNo) {
		this.serviceNo = serviceNo;
	}
	
	public SgBusArrivalInfoDTO(String serviceNo, List<String> estimatedArrivals) {	
		this.serviceNo = serviceNo;
		this.estimatedArrivals = estimatedArrivals;
	}
	public String getServiceNo() {
		return serviceNo;
	}
	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}
	public List<String> getEstimatedArrivals() {
		return estimatedArrivals;
	}
	public void setEstimatedArrivals(List<String> estimatedArrivals) {
		this.estimatedArrivals = estimatedArrivals;
	}
	
	
	
	

}
