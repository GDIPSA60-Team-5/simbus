package com.example.springbackend.model;

import java.time.LocalTime;
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
public class CommutePlan {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String commutePlanName;
	private LocalTime notifyAt;
	private LocalTime arrivalTime;
	private Integer reminderOffsetMin;
	private Boolean recurrence;
	
	@ManyToOne
    @JoinColumn(name = "userId")
    private User user;
	
    @OneToMany(mappedBy = "commutePlan", cascade = CascadeType.ALL)
    private List<CommuteHistory> commuteHistory;
    
    @OneToMany(mappedBy = "commutePlan", cascade = CascadeType.ALL)
    private List<PreferredRoute> preferredRoutes;
    
    @OneToMany(mappedBy = "commutePlan", cascade = CascadeType.ALL)
    private List<CommuteRecurrenceDay> commuteRecurrenceDays;
	
	public CommutePlan() { }
	
	public CommutePlan(String commutePlanName, LocalTime notifyAt, LocalTime arrivalTime, Integer reminderOffsetMin,
			Boolean recurrence) {
		this.commutePlanName = commutePlanName;
		this.notifyAt = notifyAt;
		this.arrivalTime = arrivalTime;
		this.reminderOffsetMin = reminderOffsetMin;
		this.recurrence = recurrence;
	}

	public String getCommutePlanName() {
		return commutePlanName;
	}

	public void setCommutePlanName(String commutePlanName) {
		this.commutePlanName = commutePlanName;
	}

	public LocalTime getNotifyAt() {
		return notifyAt;
	}

	public void setNotifyAt(LocalTime notifyAt) {
		this.notifyAt = notifyAt;
	}

	public LocalTime getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(LocalTime arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public Integer getReminderOffsetMin() {
		return reminderOffsetMin;
	}

	public void setReminderOffsetMin(Integer reminderOffsetMin) {
		this.reminderOffsetMin = reminderOffsetMin;
	}

	public Boolean getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(Boolean recurrence) {
		this.recurrence = recurrence;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<CommuteHistory> getCommuteHistory() {
		return commuteHistory;
	}

	public void setCommuteHistory(List<CommuteHistory> commuteHistory) {
		this.commuteHistory = commuteHistory;
	}

	public List<PreferredRoute> getPreferredRoute() {
		return preferredRoutes;
	}

	public void setPreferredRoute(List<PreferredRoute> preferredRoutes) {
		this.preferredRoutes = preferredRoutes;
	}
	
}
