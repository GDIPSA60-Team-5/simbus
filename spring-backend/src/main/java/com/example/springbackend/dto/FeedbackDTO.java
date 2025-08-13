package com.example.springbackend.dto;

import java.time.LocalDateTime;

public class FeedbackDTO {
    private String id;
    private String userName;
    private String userId;
    private String feedbackText;
    private Integer rating;
    private String tagList;
    private LocalDateTime submittedAt;
}
