package com.example.springbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    public void testUserBuilderAndGetters() {
        String id = "user123";
        String userName = "john_doe";
        String userType = "admin";
        String passwordHash = "hashed_password";

        User user = User.builder()
                .id(id)
                .userName(userName)
                .userType(userType)
                .passwordHash(passwordHash)
                .build();

        assertEquals(id, user.getId());
        assertEquals(userName, user.getUserName());
        assertEquals(userType, user.getUserType());
        assertEquals(passwordHash, user.getPasswordHash());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        User user = new User();

        user.setId("user456");
        user.setUserName("jane_smith");
        user.setUserType("user");
        user.setPasswordHash("another_hash");

        assertEquals("user456", user.getId());
        assertEquals("jane_smith", user.getUserName());
        assertEquals("user", user.getUserType());
        assertEquals("another_hash", user.getPasswordHash());
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        User user1 = User.builder()
                .id("user123")
                .userName("john_doe")
                .userType("admin")
                .passwordHash("hashed_password")
                .build();

        User user2 = User.builder()
                .id("user123")
                .userName("john_doe")
                .userType("admin")
                .passwordHash("hashed_password")
                .build();

        User user3 = User.builder()
                .id("user999")
                .userName("alice")
                .userType("user")
                .passwordHash("diff_hash")
                .build();

        // equals and hashCode positive
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());

        // equals and hashCode negative
        assertNotEquals(user1, user3);
        assertNotEquals(user1.hashCode(), user3.hashCode());

        // equals with null and different class
        assertNotEquals(null, user1);
        assertNotEquals("some string", user1);

        // toString test (check it contains key info)
        String toString = user1.toString();
        assertTrue(toString.contains("user123"));
        assertTrue(toString.contains("john_doe"));
        assertTrue(toString.contains("admin"));
    }
}
