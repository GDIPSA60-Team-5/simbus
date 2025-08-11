package com.example.springbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SgBusServicesAtStopDTO {
	
	private String serviceNo;
	
	
	public SgBusServicesAtStopDTO() {
		
	}
	
	public SgBusServicesAtStopDTO(String serviceNo) {
		
	}

	public String getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}

}
