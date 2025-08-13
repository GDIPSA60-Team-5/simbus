//package com.example.springbackend.model;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import lombok.Data;
//
//
//@Document(collection = "routes")
//@Data
//public class RouteMongo {
//    @Id
//    private String id;
//
//    private String userId;
//    private String from;
//    private String to;
//    private String busStop;
//    private String busService;
//    private String startTime;
//    private String arrivalTime;
//	private String notificationNum;
//    private List<Boolean> selectedDays;
//
//    @CreatedDate
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    private LocalDateTime updatedAt;
//
//    public RouteMongo(String userId, String from, String to, String busStop,
//            String busService, String startTime, String arrivalTime,String notificationNum, List<Boolean> selectedDays) {
//				this.userId = userId;
//				this.from = from;
//				this.to = to;
//				this.busStop = busStop;
//				this.busService = busService;
//				this.startTime = startTime;
//				this.arrivalTime = arrivalTime;
//				this.notificationNum = notificationNum;
//				this.selectedDays = selectedDays;
//				this.createdAt = LocalDateTime.now();
//				this.updatedAt = LocalDateTime.now();
//				}
//
//    public RouteMongo() {
//
//    	}
//
//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}
//
//	public String getUserId() {
//		return userId;
//	}
//
//	public void setUserId(String userId) {
//		this.userId = userId;
//	}
//
//	public String getFrom() {
//		return from;
//	}
//
//	public void setFrom(String from) {
//		this.from = from;
//	}
//
//	public String getTo() {
//		return to;
//	}
//
//	public void setTo(String to) {
//		this.to = to;
//	}
//
//	public String getBusStop() {
//		return busStop;
//	}
//
//	public void setBusStop(String busStop) {
//		this.busStop = busStop;
//	}
//
//	public String getBusService() {
//		return busService;
//	}
//
//	public void setBusService(String busService) {
//		this.busService = busService;
//	}
//
//	public String getStartTime() {
//		return startTime;
//	}
//
//	public void setStartTime(String startTime) {
//		this.startTime = startTime;
//	}
//
//	public String getArrivalTime() {
//		return arrivalTime;
//	}
//
//	public void setArrivalTime(String arrivalTime) {
//		this.arrivalTime = arrivalTime;
//	}
//
//	public List<Boolean> getSelectedDays() {
//		return selectedDays;
//	}
//
//	public void setSelectedDays(List<Boolean> selectedDays) {
//		this.selectedDays = selectedDays;
//	}
//
//	public LocalDateTime getCreatedAt() {
//		return createdAt;
//	}
//
//	public void setCreatedAt(LocalDateTime createdAt) {
//		this.createdAt = createdAt;
//	}
//
//	public LocalDateTime getUpdatedAt() {
//		return updatedAt;
//	}
//
//	public void setUpdatedAt(LocalDateTime updatedAt) {
//		this.updatedAt = updatedAt;
//	}
//
//
//	public String getNotificationNum() {
//		return notificationNum;
//	}
//
//	public void setNotificationNum(String notificationNum) {
//		this.notificationNum = notificationNum;
//	}
//}