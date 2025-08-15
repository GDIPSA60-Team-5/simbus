package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeedbackTest {

    private LocalDateTime now;

    @BeforeEach
    void setup() {
        now = LocalDateTime.now();
    }

    @Test
    void testBuilderAndGetters() {
        Feedback feedback = Feedback.builder()
                .id("123")
                .userId("1001")
                .feedbackText("This is great!")
                .rating(5)
                .tagList("tag1,tag2")
                .submittedAt(now)
                .build();

        assertEquals("123", feedback.getId());
        assertEquals("1001", feedback.getUserId());
        assertEquals("This is great!", feedback.getFeedbackText());
        assertEquals(5, feedback.getRating());
        assertEquals("tag1,tag2", feedback.getTagList());
        assertEquals(now, feedback.getSubmittedAt());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Feedback feedback = new Feedback();

        feedback.setId("456");
        feedback.setUserId("3003");
        feedback.setFeedbackText("Needs improvement");
        feedback.setRating(3);
        feedback.setTagList("tag3,tag4");
        feedback.setSubmittedAt(now);

        assertEquals("456", feedback.getId());
        assertEquals("3003", feedback.getUserId());
        assertEquals("Needs improvement", feedback.getFeedbackText());
        assertEquals(3, feedback.getRating());
        assertEquals("tag3,tag4", feedback.getTagList());
        assertEquals(now, feedback.getSubmittedAt());
    }

    @Test
    void testAllArgsConstructor() {
        Feedback feedback = new Feedback(
                "789",          // id
                "user5005",     // userName <-- added
                "5005",         // userId
                "Excellent service", // feedbackText
                4,              // rating
                "tag5,tag6",    // tagList
                now             // submittedAt
        );

        assertEquals("789", feedback.getId());
        assertEquals("user5005", feedback.getUserName());
        assertEquals("5005", feedback.getUserId());
        assertEquals("Excellent service", feedback.getFeedbackText());
        assertEquals(4, feedback.getRating());
        assertEquals("tag5,tag6", feedback.getTagList());
        assertEquals(now, feedback.getSubmittedAt());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        Feedback feedback1 = Feedback.builder()
                .id("abc")
                .userId("101")
                .feedbackText("Good job")
                .rating(5)
                .tagList("tagA,tagB")
                .submittedAt(now)
                .build();

        Feedback feedback2 = Feedback.builder()
                .id("abc")
                .userId("101")
                .feedbackText("Good job")
                .rating(5)
                .tagList("tagA,tagB")
                .submittedAt(now)
                .build();

        Feedback feedback3 = Feedback.builder()
                .id("def")
                .userId("102")
                .feedbackText("Needs work")
                .rating(2)
                .tagList("tagC")
                .submittedAt(now.plusDays(1))
                .build();

        assertEquals(feedback1, feedback2);
        assertEquals(feedback1.hashCode(), feedback2.hashCode());

        assertNotEquals(feedback1, feedback3);
        assertNotEquals(feedback1.hashCode(), feedback3.hashCode());

        assertNotEquals(null, feedback1);
        assertNotEquals("some string", feedback1);

        String toString = feedback1.toString();
        assertTrue(toString.contains("id=abc"));
        assertTrue(toString.contains("userId=101"));
        assertTrue(toString.contains("feedbackText=Good job"));
        assertTrue(toString.contains("rating=5"));
    }

    @Test
    public void testBuilderWithNoFieldsSet() {
        Feedback feedback = Feedback.builder().build();

        assertNull(feedback.getId());
        assertNull(feedback.getUserId());
        assertNull(feedback.getFeedbackText());
        assertNull(feedback.getRating());
        assertNull(feedback.getTagList());
        assertNull(feedback.getSubmittedAt());
    }

    @Test
    public void testEqualsWithNullFields() {
        Feedback feedback1 = new Feedback();
        Feedback feedback2 = new Feedback();

        assertEquals(feedback1, feedback2);
        assertEquals(feedback1.hashCode(), feedback2.hashCode());

        feedback2.setId("someId");
        assertNotEquals(feedback1, feedback2);
    }

    @Test
    public void testEqualsWithPartialNullAndNonNull() {
        Feedback feedback1 = new Feedback();
        feedback1.setId("abc");

        Feedback feedback2 = new Feedback();
        feedback2.setId("abc");

        assertEquals(feedback1, feedback2);

        feedback2.setId("def");
        assertNotEquals(feedback1, feedback2);
    }

    @Test
    public void testToStringHandlesNullFields() {
        Feedback feedback = new Feedback();
        String str = feedback.toString();

        assertNotNull(str);
        assertTrue(str.contains("Feedback"));
    }

    @Test
    public void testSettersAndGettersWithNulls() {
        Feedback feedback = new Feedback();

        feedback.setId(null);
        feedback.setUserId(null);
        feedback.setFeedbackText(null);
        feedback.setRating(null);
        feedback.setTagList(null);
        feedback.setSubmittedAt(null);

        assertNull(feedback.getId());
        assertNull(feedback.getUserId());
        assertNull(feedback.getFeedbackText());
        assertNull(feedback.getRating());
        assertNull(feedback.getTagList());
        assertNull(feedback.getSubmittedAt());
    }
}
