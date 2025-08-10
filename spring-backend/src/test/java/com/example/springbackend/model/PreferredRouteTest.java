package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PreferredRouteTest {

    @Test
    public void testBuilderAndGetters() {
        String id = "pref123";
        String commutePlanId = "plan456";
        String routeId = "route789";

        PreferredRoute preferredRoute = PreferredRoute.builder()
                .id(id)
                .commutePlanId(commutePlanId)
                .routeId(routeId)
                .build();

        assertEquals(id, preferredRoute.getId());
        assertEquals(commutePlanId, preferredRoute.getCommutePlanId());
        assertEquals(routeId, preferredRoute.getRouteId());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        PreferredRoute preferredRoute = new PreferredRoute();

        preferredRoute.setId("pref987");
        preferredRoute.setCommutePlanId("plan654");
        preferredRoute.setRouteId("route321");

        assertEquals("pref987", preferredRoute.getId());
        assertEquals("plan654", preferredRoute.getCommutePlanId());
        assertEquals("route321", preferredRoute.getRouteId());
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        PreferredRoute pr1 = PreferredRoute.builder()
                .id("pref123")
                .commutePlanId("plan456")
                .routeId("route789")
                .build();

        PreferredRoute pr2 = PreferredRoute.builder()
                .id("pref123")
                .commutePlanId("plan456")
                .routeId("route789")
                .build();

        PreferredRoute pr3 = PreferredRoute.builder()
                .id("pref999")
                .commutePlanId("plan000")
                .routeId("route111")
                .build();

        // equals and hashCode positive test
        assertEquals(pr1, pr2);
        assertEquals(pr1.hashCode(), pr2.hashCode());

        // equals and hashCode negative test
        assertNotEquals(pr1, pr3);
        assertNotEquals(pr1.hashCode(), pr3.hashCode());

        // equals null and different class
        assertNotEquals(null, pr1);
        assertNotEquals("some string", pr1);

        // toString contains key properties
        String toString = pr1.toString();
        assertTrue(toString.contains("pref123"));
        assertTrue(toString.contains("plan456"));
        assertTrue(toString.contains("route789"));
    }

    @Test
    public void testBuilderWithNoFieldsSet() {
        PreferredRoute pr = PreferredRoute.builder().build();

        assertNull(pr.getId());
        assertNull(pr.getCommutePlanId());
        assertNull(pr.getRouteId());
    }

    @Test
    public void testEqualsWithNullFields() {
        PreferredRoute pr1 = new PreferredRoute();
        PreferredRoute pr2 = new PreferredRoute();

        assertEquals(pr1, pr2);
        assertEquals(pr1.hashCode(), pr2.hashCode());

        pr2.setId("id123");
        assertNotEquals(pr1, pr2);
    }

    @Test
    public void testEqualsWithPartialNullAndNonNull() {
        PreferredRoute pr1 = new PreferredRoute();
        pr1.setId("abc");

        PreferredRoute pr2 = new PreferredRoute();
        pr2.setId("abc");

        assertEquals(pr1, pr2);

        pr2.setId("def");
        assertNotEquals(pr1, pr2);
    }

    @Test
    public void testToStringWithNullFields() {
        PreferredRoute pr = new PreferredRoute();
        String str = pr.toString();

        assertNotNull(str);
        assertTrue(str.contains("PreferredRoute"));
    }

    @Test
    public void testSettersAndGettersWithNull() {
        PreferredRoute pr = new PreferredRoute();

        pr.setId(null);
        pr.setCommutePlanId(null);
        pr.setRouteId(null);

        assertNull(pr.getId());
        assertNull(pr.getCommutePlanId());
        assertNull(pr.getRouteId());
    }
}
