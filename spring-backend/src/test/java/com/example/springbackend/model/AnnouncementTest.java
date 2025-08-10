package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

public class AnnouncementTest {

    @Test
    public void testAnnouncementBuilderAndGetters() {
        String id = "ann123";
        String title = "System Maintenance";
        String content = "The system will be down tonight from 12 AM to 2 AM.";
        Instant createdAt = Instant.parse("2025-08-10T10:00:00Z");
        Instant expiresAt = Instant.parse("2025-08-11T10:00:00Z");
        String userId = "user789";

        Announcement announcement = Announcement.builder()
                .id(id)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .userId(userId)
                .build();

        assertEquals(id, announcement.getId());
        assertEquals(title, announcement.getTitle());
        assertEquals(content, announcement.getContent());
        assertEquals(createdAt, announcement.getCreatedAt());
        assertEquals(expiresAt, announcement.getExpiresAt());
        assertEquals(userId, announcement.getUserId());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        Announcement announcement = new Announcement();

        announcement.setId("ann456");
        announcement.setTitle("New Feature");
        announcement.setContent("We launched a new dashboard feature.");
        announcement.setCreatedAt(Instant.parse("2025-08-09T09:30:00Z"));
        announcement.setExpiresAt(Instant.parse("2025-08-15T09:30:00Z"));
        announcement.setUserId("user123");

        assertEquals("ann456", announcement.getId());
        assertEquals("New Feature", announcement.getTitle());
        assertEquals("We launched a new dashboard feature.", announcement.getContent());
        assertEquals(Instant.parse("2025-08-09T09:30:00Z"), announcement.getCreatedAt());
        assertEquals(Instant.parse("2025-08-15T09:30:00Z"), announcement.getExpiresAt());
        assertEquals("user123", announcement.getUserId());
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        Announcement a1 = Announcement.builder()
                .id("ann123")
                .title("System Maintenance")
                .content("The system will be down tonight from 12 AM to 2 AM.")
                .createdAt(Instant.parse("2025-08-10T10:00:00Z"))
                .expiresAt(Instant.parse("2025-08-11T10:00:00Z"))
                .userId("user789")
                .build();

        Announcement a2 = Announcement.builder()
                .id("ann123")
                .title("System Maintenance")
                .content("The system will be down tonight from 12 AM to 2 AM.")
                .createdAt(Instant.parse("2025-08-10T10:00:00Z"))
                .expiresAt(Instant.parse("2025-08-11T10:00:00Z"))
                .userId("user789")
                .build();

        Announcement a3 = Announcement.builder()
                .id("ann999")
                .title("Other Announcement")
                .content("Different content")
                .createdAt(Instant.parse("2025-08-12T12:00:00Z"))
                .expiresAt(Instant.parse("2025-08-13T12:00:00Z"))
                .userId("user000")
                .build();

        // equals and hashCode positive test
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        // equals and hashCode negative test
        assertNotEquals(a1, a3);
        assertNotEquals(a1.hashCode(), a3.hashCode());

        // equals null and different class
        assertNotEquals(null, a1);
        assertNotEquals("some string", a1);

        // toString contains key fields
        String str = a1.toString();
        assertTrue(str.contains("ann123"));
        assertTrue(str.contains("System Maintenance"));
        assertTrue(str.contains("user789"));
    }

    @Test
    public void testBuilderWithNoFieldsSet() {
        Announcement announcement = Announcement.builder().build();

        assertNull(announcement.getId());
        assertNull(announcement.getTitle());
        assertNull(announcement.getContent());
        assertNull(announcement.getCreatedAt());
        assertNull(announcement.getExpiresAt());
        assertNull(announcement.getUserId());
    }

    @Test
    public void testEqualsWithNullFields() {
        Announcement a1 = new Announcement();
        Announcement a2 = new Announcement();

        // Both empty should be equal
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        // Change one field
        a2.setId("someId");
        assertNotEquals(a1, a2);
    }

    @Test
    public void testEqualsWithPartialNullAndNonNull() {
        Announcement a1 = new Announcement();
        a1.setId("abc");

        Announcement a2 = new Announcement();
        a2.setId("abc");

        assertEquals(a1, a2);

        a2.setId("def");
        assertNotEquals(a1, a2);
    }

    @Test
    public void testToStringWithNullFields() {
        Announcement a = new Announcement();
        String str = a.toString();

        assertNotNull(str);
        assertTrue(str.contains("Announcement"));
    }

    @Test
    public void testSettersAndGettersWithNull() {
        Announcement a = new Announcement();

        a.setId(null);
        a.setTitle(null);
        a.setContent(null);
        a.setCreatedAt(null);
        a.setExpiresAt(null);
        a.setUserId(null);

        assertNull(a.getId());
        assertNull(a.getTitle());
        assertNull(a.getContent());
        assertNull(a.getCreatedAt());
        assertNull(a.getExpiresAt());
        assertNull(a.getUserId());
    }
}
