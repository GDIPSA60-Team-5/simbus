package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FavoriteLocationTest {

    @Test
    public void testFavoriteLocationBuilderAndGetters() {
        String id = "favLoc123";
        String locationName = "Home";
        Double latitude = 1.3521;
        Double longitude = 103.8198;
        String userId = "user789";

        FavoriteLocation favoriteLocation = FavoriteLocation.builder()
                .id(id)
                .locationName(locationName)
                .latitude(latitude)
                .longitude(longitude)
                .userId(userId)
                .build();

        assertEquals(id, favoriteLocation.getId());
        assertEquals(locationName, favoriteLocation.getLocationName());
        assertEquals(latitude, favoriteLocation.getLatitude());
        assertEquals(longitude, favoriteLocation.getLongitude());
        assertEquals(userId, favoriteLocation.getUserId());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        FavoriteLocation favoriteLocation = new FavoriteLocation();

        favoriteLocation.setId("favLoc456");
        favoriteLocation.setLocationName("Work");
        favoriteLocation.setLatitude(40.7128);
        favoriteLocation.setLongitude(-74.0060);
        favoriteLocation.setUserId("user123");

        assertEquals("favLoc456", favoriteLocation.getId());
        assertEquals("Work", favoriteLocation.getLocationName());
        assertEquals(40.7128, favoriteLocation.getLatitude());
        assertEquals(-74.0060, favoriteLocation.getLongitude());
        assertEquals("user123", favoriteLocation.getUserId());
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        FavoriteLocation f1 = FavoriteLocation.builder()
                .id("favLoc123")
                .locationName("Home")
                .latitude(1.3521)
                .longitude(103.8198)
                .userId("user789")
                .build();

        FavoriteLocation f2 = FavoriteLocation.builder()
                .id("favLoc123")
                .locationName("Home")
                .latitude(1.3521)
                .longitude(103.8198)
                .userId("user789")
                .build();

        FavoriteLocation f3 = FavoriteLocation.builder()
                .id("favLoc999")
                .locationName("Work")
                .latitude(40.7128)
                .longitude(-74.0060)
                .userId("user123")
                .build();

        // equals and hashCode positive test
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());

        // equals and hashCode negative test
        assertNotEquals(f1, f3);
        assertNotEquals(f1.hashCode(), f3.hashCode());

        // equals null and different class
        assertNotEquals(null, f1);
        assertNotEquals("some string", f1);

        // toString contains key fields
        String str = f1.toString();
        assertTrue(str.contains("favLoc123"));
        assertTrue(str.contains("Home"));
        assertTrue(str.contains("user789"));
    }
}
