package com.example.springbackend.dto;

public class NusBusArrivalInfoDTO {
	private String serviceName;
	private String arrivalTime;
	private String nextArrivalTime;
	
	public NusBusArrivalInfoDTO() {
	}

	public NusBusArrivalInfoDTO(String serviceName, String arrivalTime, String nextArrivalTime) {
		this.serviceName = serviceName;
		this.arrivalTime = arrivalTime;
		this.nextArrivalTime = nextArrivalTime;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getNextArrivalTime() {
		return nextArrivalTime;
	}

	public void setNextArrivalTime(String nextArrivalTime) {
		this.nextArrivalTime = nextArrivalTime;
	}

}
