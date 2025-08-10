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
}

