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
}
