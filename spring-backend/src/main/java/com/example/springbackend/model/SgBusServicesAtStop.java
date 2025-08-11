package com.example.springbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SgBusServicesAtStop {
	@JsonProperty("ServiceNo")
	private String serviceNo;
	
	public SgBusServicesAtStop() {
	}
	
	
	public SgBusServicesAtStop(String serviceNo) {
		this.serviceNo = serviceNo;
	}

	public String getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}
	
	

}