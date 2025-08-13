package com.example.springbackend.model;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
	@Id
	private String id;

	private String userName;
	private String userId;
	private String feedbackText;
	private Integer rating;
	private String tagList;
	private LocalDateTime submittedAt;
}
