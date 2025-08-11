package com.example.springbackend.dto;

public class SgBusStopDTO {
	
	private String busStopCode;
	private String roadName;
	private String latitude;
	private String longitude;
	
	public SgBusStopDTO() {
		
	}

	public SgBusStopDTO(String busStopCode, String roadName, String latitude, String longitude) {
		this.busStopCode = busStopCode;
		this.roadName = roadName;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getBusStopCode() {
		return busStopCode;
	}

	public void setBusStopCode(String busStopCode) {
		this.busStopCode = busStopCode;
	}

	public String getRoadName() {
		return roadName;
	}

	public void setRoadName(String roadName) {
		this.roadName = roadName;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

}
