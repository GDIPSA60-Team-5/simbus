package com.example.springbackend.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Users")
public class User {
	
	@Id
	@GeneratedValue
	private UUID id;
	
	private String userName;
	private String userType;
	private String passwordHash;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Location> locations;
	
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CommutePlan> commutePlans;
	
	public User() { }
	
	public User(UUID id, String userName, String userType, String passwordHash) {
		this.userName = userName;
		this.userType = userType;
		this.passwordHash = passwordHash;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public UUID getId() {
		return id;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public List<CommutePlan> getCommutePlans() {
		return commutePlans;
	}

	public void setCommutePlans(List<CommutePlan> commutePlans) {
		this.commutePlans = commutePlans;
	}

}
