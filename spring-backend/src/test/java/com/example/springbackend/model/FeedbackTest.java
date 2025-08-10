package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class FeedbackTest {

    @Test
    void testBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();

        Feedback feedback = Feedback.builder()
                .id("123")
                .userId(1001L)
                .historyId(2002L)
                .feedbackText("This is great!")
                .rating(5)
                .tagList("tag1,tag2")
                .submittedAt(now)
                .build();

        assertEquals("123", feedback.getId());
        assertEquals(1001L, feedback.getUserId());
        assertEquals(2002L, feedback.getHistoryId());
        assertEquals("This is great!", feedback.getFeedbackText());
        assertEquals(5, feedback.getRating());
        assertEquals("tag1,tag2", feedback.getTagList());
        assertEquals(now, feedback.getSubmittedAt());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Feedback feedback = new Feedback();
        LocalDateTime now = LocalDateTime.now();

        feedback.setId("456");
        feedback.setUserId(3003L);
        feedback.setHistoryId(4004L);
        feedback.setFeedbackText("Needs improvement");
        feedback.setRating(3);
        feedback.setTagList("tag3,tag4");
        feedback.setSubmittedAt(now);

        assertEquals("456", feedback.getId());
        assertEquals(3003L, feedback.getUserId());
        assertEquals(4004L, feedback.getHistoryId());
        assertEquals("Needs improvement", feedback.getFeedbackText());
        assertEquals(3, feedback.getRating());
        assertEquals("tag3,tag4", feedback.getTagList());
        assertEquals(now, feedback.getSubmittedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        Feedback feedback = new Feedback(
                "789",
                5005L,
                6006L,
                "Excellent service",
                4,
                "tag5,tag6",
                now);

        assertEquals("789", feedback.getId());
        assertEquals(5005L, feedback.getUserId());
        assertEquals(6006L, feedback.getHistoryId());
        assertEquals("Excellent service", feedback.getFeedbackText());
        assertEquals(4, feedback.getRating());
        assertEquals("tag5,tag6", feedback.getTagList());
        assertEquals(now, feedback.getSubmittedAt());
    }
}
