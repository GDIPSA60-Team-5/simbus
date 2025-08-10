package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CommutePlanTest {

    @Test
    public void testCommutePlanBuilderAndGetters() {
        String id = "123";
        String commutePlanName = "Morning Commute";
        LocalTime notifyAt = LocalTime.of(7, 30);
        LocalTime arrivalTime = LocalTime.of(8, 30);
        Integer reminderOffsetMin = 15;
        Boolean recurrence = true;
        String startLocationId = "loc1";
        String endLocationId = "loc2";
        String userId = "user123";
        var commuteHistoryIds = Arrays.asList("hist1", "hist2");
        var preferredRouteIds = List.of("route1");
        var commuteRecurrenceDayIds = Arrays.asList("day1", "day2", "day3");

        CommutePlan plan = CommutePlan.builder()
                .id(id)
                .commutePlanName(commutePlanName)
                .notifyAt(notifyAt)
                .arrivalTime(arrivalTime)
                .reminderOffsetMin(reminderOffsetMin)
                .recurrence(recurrence)
                .startLocationId(startLocationId)
                .endLocationId(endLocationId)
                .userId(userId)
                .commuteHistoryIds(commuteHistoryIds)
                .preferredRouteIds(preferredRouteIds)
                .commuteRecurrenceDayIds(commuteRecurrenceDayIds)
                .build();

        assertEquals(id, plan.getId());
        assertEquals(commutePlanName, plan.getCommutePlanName());
        assertEquals(notifyAt, plan.getNotifyAt());
        assertEquals(arrivalTime, plan.getArrivalTime());
        assertEquals(reminderOffsetMin, plan.getReminderOffsetMin());
        assertEquals(recurrence, plan.getRecurrence());
        assertEquals(startLocationId, plan.getStartLocationId());
        assertEquals(endLocationId, plan.getEndLocationId());
        assertEquals(userId, plan.getUserId());
        assertEquals(commuteHistoryIds, plan.getCommuteHistoryIds());
        assertEquals(preferredRouteIds, plan.getPreferredRouteIds());
        assertEquals(commuteRecurrenceDayIds, plan.getCommuteRecurrenceDayIds());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        CommutePlan plan = getCommutePlan();

        assertEquals("id123", plan.getId());
        assertEquals("Evening Commute", plan.getCommutePlanName());
        assertEquals(LocalTime.of(18, 0), plan.getNotifyAt());
        assertEquals(LocalTime.of(19, 0), plan.getArrivalTime());
        assertEquals(10, plan.getReminderOffsetMin());
        assertFalse(plan.getRecurrence());
        assertEquals("startLoc", plan.getStartLocationId());
        assertEquals("endLoc", plan.getEndLocationId());
        assertEquals("user456", plan.getUserId());
        assertEquals(List.of("histA"), plan.getCommuteHistoryIds());
        assertEquals(List.of("routeB"), plan.getPreferredRouteIds());
        assertEquals(List.of("dayX"), plan.getCommuteRecurrenceDayIds());
    }

    private static CommutePlan getCommutePlan() {
        CommutePlan plan = new CommutePlan();

        plan.setId("id123");
        plan.setCommutePlanName("Evening Commute");
        plan.setNotifyAt(LocalTime.of(18, 0));
        plan.setArrivalTime(LocalTime.of(19, 0));
        plan.setReminderOffsetMin(10);
        plan.setRecurrence(false);
        plan.setStartLocationId("startLoc");
        plan.setEndLocationId("endLoc");
        plan.setUserId("user456");
        plan.setCommuteHistoryIds(List.of("histA"));
        plan.setPreferredRouteIds(List.of("routeB"));
        plan.setCommuteRecurrenceDayIds(List.of("dayX"));
        return plan;
    }

    @Test
    public void testEqualsAndHashCode() {
        CommutePlan plan1 = CommutePlan.builder()
                .id("plan123")
                .commutePlanName("Morning")
                .notifyAt(LocalTime.of(7, 0))
                .arrivalTime(LocalTime.of(8, 0))
                .reminderOffsetMin(5)
                .recurrence(true)
                .startLocationId("start1")
                .endLocationId("end1")
                .userId("user1")
                .commuteHistoryIds(List.of("hist1"))
                .preferredRouteIds(List.of("route1"))
                .commuteRecurrenceDayIds(List.of("day1"))
                .build();

        CommutePlan plan2 = CommutePlan.builder()
                .id("plan123")
                .commutePlanName("Morning")
                .notifyAt(LocalTime.of(7, 0))
                .arrivalTime(LocalTime.of(8, 0))
                .reminderOffsetMin(5)
                .recurrence(true)
                .startLocationId("start1")
                .endLocationId("end1")
                .userId("user1")
                .commuteHistoryIds(List.of("hist1"))
                .preferredRouteIds(List.of("route1"))
                .commuteRecurrenceDayIds(List.of("day1"))
                .build();

        CommutePlan plan3 = CommutePlan.builder()
                .id("plan999")
                .commutePlanName("Evening")
                .notifyAt(LocalTime.of(18, 0))
                .arrivalTime(LocalTime.of(19, 0))
                .reminderOffsetMin(10)
                .recurrence(false)
                .startLocationId("start2")
                .endLocationId("end2")
                .userId("user2")
                .commuteHistoryIds(List.of("hist2"))
                .preferredRouteIds(List.of("route2"))
                .commuteRecurrenceDayIds(List.of("day2"))
                .build();

        // Positive test equals/hashCode
        assertEquals(plan1, plan2);
        assertEquals(plan1.hashCode(), plan2.hashCode());

        // Negative test equals/hashCode
        assertNotEquals(plan1, plan3);
        assertNotEquals(plan1.hashCode(), plan3.hashCode());

        // equals with null and different class
        assertNotEquals(null, plan1);
        assertNotEquals("some string", plan1);
    }
}
