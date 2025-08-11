package com.example.springbackend.dto;

public class NusBusStopDTO {
	
	private String name;
	private String longName;
	private double latitude;
	private double longitude;
	
	public NusBusStopDTO() {
		
	}

	public NusBusStopDTO(String name, String longName, double latitude, double longitude) {
		this.name = name;
		this.longName = longName;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	

}
