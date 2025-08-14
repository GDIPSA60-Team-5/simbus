//package com.example.springbackend.model;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//
//public class CommutePlanTest {
//
//    @Test
//    public void testCommutePlanBuilderAndGetters() {
//        String id = "123";
//        String commutePlanName = "Morning Commute";
//        LocalTime notifyAt = LocalTime.of(7, 30);
//        LocalTime arrivalTime = LocalTime.of(8, 30);
//        Integer reminderOffsetMin = 15;
//        Boolean recurrence = true;
//        String startLocationId = "loc1";
//        String endLocationId = "loc2";
//        String userId = "user123";
//        var commuteHistoryIds = Arrays.asList("hist1", "hist2");
//        var preferredRouteIds = List.of("route1");
//        var commuteRecurrenceDayIds = Arrays.asList("day1", "day2", "day3");
//
//        CommutePlan plan = CommutePlan.builder()
//                .id(id)
//                .commutePlanName(commutePlanName)
//                .notifyAt(notifyAt)
//                .arrivalTime(arrivalTime)
//                .reminderOffsetMin(reminderOffsetMin)
//                .recurrence(recurrence)
//                .startLocationId(startLocationId)
//                .endLocationId(endLocationId)
//                .userId(userId)
//                .commuteRecurrenceDayIds(commuteRecurrenceDayIds)
//                .build();
//
//        assertEquals(id, plan.getId());
//        assertEquals(commutePlanName, plan.getCommutePlanName());
//        assertEquals(notifyAt, plan.getNotifyAt());
//        assertEquals(arrivalTime, plan.getArrivalTime());
//        assertEquals(reminderOffsetMin, plan.getReminderOffsetMin());
//        assertEquals(recurrence, plan.getRecurrence());
//        assertEquals(startLocationId, plan.getStartLocationId());
//        assertEquals(endLocationId, plan.getEndLocationId());
//        assertEquals(userId, plan.getUserId());
//        assertEquals(commuteRecurrenceDayIds, plan.getCommuteRecurrenceDayIds());
//    }
//
//    @Test
//    public void testNoArgsConstructorAndSetters() {
//        CommutePlan plan = getCommutePlan();
//
//        assertEquals("id123", plan.getId());
//        assertEquals("Evening Commute", plan.getCommutePlanName());
//        assertEquals(LocalTime.of(18, 0), plan.getNotifyAt());
//        assertEquals(LocalTime.of(19, 0), plan.getArrivalTime());
//        assertEquals(10, plan.getReminderOffsetMin());
//        assertFalse(plan.getRecurrence());
//        assertEquals("startLoc", plan.getStartLocationId());
//        assertEquals("endLoc", plan.getEndLocationId());
//        assertEquals("user456", plan.getUserId());
//        assertEquals(List.of("dayX"), plan.getCommuteRecurrenceDayIds());
//    }
//
//    private static CommutePlan getCommutePlan() {
//        CommutePlan plan = new CommutePlan();
//
//        plan.setId("id123");
//        plan.setCommutePlanName("Evening Commute");
//        plan.setNotifyAt(LocalTime.of(18, 0));
//        plan.setArrivalTime(LocalTime.of(19, 0));
//        plan.setReminderOffsetMin(10);
//        plan.setRecurrence(false);
//        plan.setStartLocationId("startLoc");
//        plan.setEndLocationId("endLoc");
//        plan.setUserId("user456");
//        plan.setCommuteRecurrenceDayIds(List.of("dayX"));
//        return plan;
//    }
//
//    @Test
//    public void testEqualsAndHashCode() {
//        CommutePlan plan1 = CommutePlan.builder()
//                .id("plan123")
//                .commutePlanName("Morning")
//                .notifyAt(LocalTime.of(7, 0))
//                .arrivalTime(LocalTime.of(8, 0))
//                .reminderOffsetMin(5)
//                .recurrence(true)
//                .startLocationId("start1")
//                .endLocationId("end1")
//                .userId("user1")
//                .commuteRecurrenceDayIds(List.of("day1"))
//                .build();
//
//        CommutePlan plan2 = CommutePlan.builder()
//                .id("plan123")
//                .commutePlanName("Morning")
//                .notifyAt(LocalTime.of(7, 0))
//                .arrivalTime(LocalTime.of(8, 0))
//                .reminderOffsetMin(5)
//                .recurrence(true)
//                .startLocationId("start1")
//                .endLocationId("end1")
//                .userId("user1")
//                .commuteRecurrenceDayIds(List.of("day1"))
//                .build();
//
//        CommutePlan plan3 = CommutePlan.builder()
//                .id("plan999")
//                .commutePlanName("Evening")
//                .notifyAt(LocalTime.of(18, 0))
//                .arrivalTime(LocalTime.of(19, 0))
//                .reminderOffsetMin(10)
//                .recurrence(false)
//                .startLocationId("start2")
//                .endLocationId("end2")
//                .userId("user2")
//                .commuteRecurrenceDayIds(List.of("day2"))
//                .build();
//
//        // Positive test equals/hashCode
//        assertEquals(plan1, plan2);
//        assertEquals(plan1.hashCode(), plan2.hashCode());
//
//        // Negative test equals/hashCode
//        assertNotEquals(plan1, plan3);
//        assertNotEquals(plan1.hashCode(), plan3.hashCode());
//
//        // equals with null and different class
//        assertNotEquals(null, plan1);
//        assertNotEquals("some string", plan1);
//    }
//
//    @Test
//    public void testBuilderWithNoFieldsSet() {
//        CommutePlan plan = CommutePlan.builder().build();
//
//        assertNull(plan.getId());
//        assertNull(plan.getCommutePlanName());
//        assertNull(plan.getNotifyAt());
//        assertNull(plan.getArrivalTime());
//        assertNull(plan.getReminderOffsetMin());
//        assertNull(plan.getRecurrence());
//        assertNull(plan.getStartLocationId());
//        assertNull(plan.getEndLocationId());
//        assertNull(plan.getUserId());
//        assertNull(plan.getCommuteRecurrenceDayIds());
//    }
//
//    @Test
//    public void testEqualsWithNullFields() {
//        CommutePlan plan1 = new CommutePlan();
//        CommutePlan plan2 = new CommutePlan();
//
//        // Both empty objects should be equal
//        assertEquals(plan1, plan2);
//        assertEquals(plan1.hashCode(), plan2.hashCode());
//
//        // One with null lists, other with empty lists should NOT be equal
//        assertNotEquals(plan1, plan2);
//    }
//
//    @Test
//    public void testEqualsWithPartialNullAndNonNull() {
//        CommutePlan plan1 = new CommutePlan();
//        plan1.setId("abc");
//
//        CommutePlan plan2 = new CommutePlan();
//        plan2.setId("abc");
//
//        assertEquals(plan1, plan2);
//
//        plan2.setId("def");
//        assertNotEquals(plan1, plan2);
//    }
//
//    @Test
//    public void testToStringNotNull() {
//        CommutePlan plan = getSamplePlanWithNulls();
//        String str = plan.toString();
//
//        // toString should include class name and id if set or "null" placeholders
//        assertNotNull(str);
//        assertTrue(str.contains("CommutePlan"));
//    }
//
//    @Test
//    public void testListFieldsWithEmptyAndNull() {
//        CommutePlan plan = new CommutePlan();
//
//        // Set lists to empty and null and check getter returns
//        plan.setCommuteRecurrenceDayIds(new ArrayList<>());
//
//        assertTrue(plan.getCommuteRecurrenceDayIds().isEmpty());
//    }
//
//    private CommutePlan getSamplePlanWithNulls() {
//        return CommutePlan.builder()
//                .id(null)
//                .commutePlanName(null)
//                .notifyAt(null)
//                .arrivalTime(null)
//                .reminderOffsetMin(null)
//                .recurrence(null)
//                .startLocationId(null)
//                .endLocationId(null)
//                .userId(null)
//                .commuteRecurrenceDayIds(null)
//                .build();
//    }
//
//    @Test
//    public void testSetRecurrenceNull() {
//        CommutePlan plan = new CommutePlan();
//        plan.setRecurrence(null);
//        assertNull(plan.getRecurrence());
//    }
//
//    @Test
//    public void testEqualsWithDifferentListOrder() {
//        List<String> list1 = Arrays.asList("a", "b", "c");
//        List<String> list2 = Arrays.asList("c", "b", "a");
//
//        CommutePlan plan1 = CommutePlan.builder()
//                .commuteRecurrenceDayIds(list1)
//                .build();
//
//        CommutePlan plan2 = CommutePlan.builder()
//                .commuteRecurrenceDayIds(list2)
//                .build();
//
//        // Lists with same elements but different order are not equal
//        assertNotEquals(plan1, plan2);
//    }
//
//    @Test
//    public void testHashCodeWithNullFields() {
//        CommutePlan plan = new CommutePlan();
//        // Should not throw NPE
//        int hc = plan.hashCode();
//        assertTrue(hc != 0); // just verify hashcode runs
//    }
//
//    @Test
//    public void testToStringIncludesFields() {
//        CommutePlan plan = CommutePlan.builder()
//                .id("plan123")
//                .commutePlanName("Test Plan")
//                .build();
//
//        String str = plan.toString();
//        assertTrue(str.contains("plan123"));
//        assertTrue(str.contains("Test Plan"));
//    }
//}
