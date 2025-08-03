package com.example.springbackend.model;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;

@Document
public class Notification {

	@Id
	private String id;
	
	private Long userID;
    private String type;
    private String title;
    private String message;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
    
    public Notification() { }
    
	public Notification(Long userID, String type, String title, String message, LocalDateTime sentAt,
			LocalDateTime expiresAt) {
		this.userID = userID;
		this.type = type;
		this.title = title;
		this.message = message;
		this.sentAt = sentAt;
		this.expiresAt = expiresAt;
	}

	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getId() {
		return id;
	}
    
}
