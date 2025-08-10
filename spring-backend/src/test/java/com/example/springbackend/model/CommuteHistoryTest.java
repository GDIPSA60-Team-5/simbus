package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class CommuteHistoryTest {

    @Test
    public void testCommuteHistoryBuilderAndGetters() {
        String id = "history123";
        String status = "COMPLETED";
        LocalDateTime startedAt = LocalDateTime.of(2025, 8, 10, 8, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 8, 10, 8, 45);
        String commutePlanId = "plan456";
        String routeId = "route789";

        CommuteHistory history = CommuteHistory.builder()
                .id(id)
                .status(status)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .commutePlanId(commutePlanId)
                .routeId(routeId)
                .build();

        assertEquals(id, history.getId());
        assertEquals(status, history.getStatus());
        assertEquals(startedAt, history.getStartedAt());
        assertEquals(endedAt, history.getEndedAt());
        assertEquals(commutePlanId, history.getCommutePlanId());
        assertEquals(routeId, history.getRouteId());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        CommuteHistory history = new CommuteHistory();

        history.setId("history789");
        history.setStatus("IN_PROGRESS");
        history.setStartedAt(LocalDateTime.of(2025, 8, 10, 9, 0));
        history.setEndedAt(LocalDateTime.of(2025, 8, 10, 9, 30));
        history.setCommutePlanId("plan123");
        history.setRouteId("route456");

        assertEquals("history789", history.getId());
        assertEquals("IN_PROGRESS", history.getStatus());
        assertEquals(LocalDateTime.of(2025, 8, 10, 9, 0), history.getStartedAt());
        assertEquals(LocalDateTime.of(2025, 8, 10, 9, 30), history.getEndedAt());
        assertEquals("plan123", history.getCommutePlanId());
        assertEquals("route456", history.getRouteId());
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        CommuteHistory history1 = CommuteHistory.builder()
                .id("history123")
                .status("COMPLETED")
                .startedAt(LocalDateTime.of(2025, 8, 10, 8, 0))
                .endedAt(LocalDateTime.of(2025, 8, 10, 8, 45))
                .commutePlanId("plan456")
                .routeId("route789")
                .build();

        CommuteHistory history2 = CommuteHistory.builder()
                .id("history123")
                .status("COMPLETED")
                .startedAt(LocalDateTime.of(2025, 8, 10, 8, 0))
                .endedAt(LocalDateTime.of(2025, 8, 10, 8, 45))
                .commutePlanId("plan456")
                .routeId("route789")
                .build();

        CommuteHistory history3 = CommuteHistory.builder()
                .id("history999")
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.of(2025, 8, 10, 9, 0))
                .endedAt(LocalDateTime.of(2025, 8, 10, 9, 30))
                .commutePlanId("plan123")
                .routeId("route456")
                .build();

        // Positive tests for equals and hashCode
        assertEquals(history1, history2);
        assertEquals(history1.hashCode(), history2.hashCode());

        // Negative tests for equals and hashCode
        assertNotEquals(history1, history3);
        assertNotEquals(history1.hashCode(), history3.hashCode());

        // equals with null and different class
        assertNotEquals(null, history1);
        assertNotEquals("some string", history1);

        // toString test (just check contains some key values)
        String toString = history1.toString();
        assertTrue(toString.contains("history123"));
        assertTrue(toString.contains("COMPLETED"));
        assertTrue(toString.contains("plan456"));
    }

    @Test
    public void testBuilderWithNoFieldsSet() {
        CommuteHistory history = CommuteHistory.builder().build();

        assertNull(history.getId());
        assertNull(history.getStatus());
        assertNull(history.getStartedAt());
        assertNull(history.getEndedAt());
        assertNull(history.getCommutePlanId());
        assertNull(history.getRouteId());
    }

    @Test
    public void testEqualsWithNullFields() {
        CommuteHistory history1 = new CommuteHistory();
        CommuteHistory history2 = new CommuteHistory();

        // Both empty objects should be equal
        assertEquals(history1, history2);
        assertEquals(history1.hashCode(), history2.hashCode());

        // Change one field in one object
        history2.setId("someId");
        assertNotEquals(history1, history2);
    }

    @Test
    public void testEqualsWithPartialNullAndNonNull() {
        CommuteHistory history1 = new CommuteHistory();
        history1.setId("abc");

        CommuteHistory history2 = new CommuteHistory();
        history2.setId("abc");

        assertEquals(history1, history2);

        history2.setId("def");
        assertNotEquals(history1, history2);
    }

    @Test
    public void testToStringHandlesNullFields() {
        CommuteHistory history = new CommuteHistory();
        String str = history.toString();

        assertNotNull(str);
        assertTrue(str.contains("CommuteHistory"));
    }

    @Test
    public void testSettersAndGettersWithNulls() {
        CommuteHistory history = new CommuteHistory();

        history.setId(null);
        history.setStatus(null);
        history.setStartedAt(null);
        history.setEndedAt(null);
        history.setCommutePlanId(null);
        history.setRouteId(null);

        assertNull(history.getId());
        assertNull(history.getStatus());
        assertNull(history.getStartedAt());
        assertNull(history.getEndedAt());
        assertNull(history.getCommutePlanId());
        assertNull(history.getRouteId());
    }
}

