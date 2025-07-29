package com.example.springbackend.model;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;

@Document
public class Feedback {
	
	@Id
	private String id;
	
	private Long userId;
	private Long historyId;
	private String feedbackText;
    private Integer rating;
    private String tagList;
    private LocalDateTime submittedAt;
    
    public Feedback() { }
    
	public Feedback(Long userId, Long historyId, String feedbackText, Integer rating, String tagList,
			LocalDateTime submittedAt) {
		this.userId = userId;
		this.historyId = historyId;
		this.feedbackText = feedbackText;
		this.rating = rating;
		this.tagList = tagList;
		this.submittedAt = submittedAt;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getHistoryId() {
		return historyId;
	}

	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}

	public String getFeedbackText() {
		return feedbackText;
	}

	public void setFeedbackText(String feedbackText) {
		this.feedbackText = feedbackText;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getTagList() {
		return tagList;
	}

	public void setTagList(String tagList) {
		this.tagList = tagList;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public String getId() {
		return id;
	}
    
}
