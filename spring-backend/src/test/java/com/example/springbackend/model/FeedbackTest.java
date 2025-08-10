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

    @Test
    void testEqualsHashCodeAndToString() {
        LocalDateTime now = LocalDateTime.now();

        Feedback feedback1 = Feedback.builder()
                .id("abc")
                .userId(101L)
                .historyId(201L)
                .feedbackText("Good job")
                .rating(5)
                .tagList("tagA,tagB")
                .submittedAt(now)
                .build();

        Feedback feedback2 = Feedback.builder()
                .id("abc") // same id and fields for equality
                .userId(101L)
                .historyId(201L)
                .feedbackText("Good job")
                .rating(5)
                .tagList("tagA,tagB")
                .submittedAt(now)
                .build();

        Feedback feedback3 = Feedback.builder()
                .id("def") // different id and fields for inequality
                .userId(102L)
                .historyId(202L)
                .feedbackText("Needs work")
                .rating(2)
                .tagList("tagC")
                .submittedAt(now.plusDays(1))
                .build();

        // equals and hashCode positive case
        assertEquals(feedback1, feedback2);
        assertEquals(feedback1.hashCode(), feedback2.hashCode());

        // equals and hashCode negative case
        assertNotEquals(feedback1, feedback3);
        assertNotEquals(feedback1.hashCode(), feedback3.hashCode());

        // equals null and different class
        assertNotEquals(null, feedback1);
        assertNotEquals("some string", feedback1);

        // toString contains key field values
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
        assertNull(feedback.getHistoryId());
        assertNull(feedback.getFeedbackText());
        assertNull(feedback.getRating());
        assertNull(feedback.getTagList());
        assertNull(feedback.getSubmittedAt());
    }

    @Test
    public void testEqualsWithNullFields() {
        Feedback feedback1 = new Feedback();
        Feedback feedback2 = new Feedback();

        // Both empty objects should be equal
        assertEquals(feedback1, feedback2);
        assertEquals(feedback1.hashCode(), feedback2.hashCode());

        // Change one field to non-null to test inequality
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
        feedback.setHistoryId(null);
        feedback.setFeedbackText(null);
        feedback.setRating(null);
        feedback.setTagList(null);
        feedback.setSubmittedAt(null);

        assertNull(feedback.getId());
        assertNull(feedback.getUserId());
        assertNull(feedback.getHistoryId());
        assertNull(feedback.getFeedbackText());
        assertNull(feedback.getRating());
        assertNull(feedback.getTagList());
        assertNull(feedback.getSubmittedAt());
    }
}
