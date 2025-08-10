package com.example.springbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() throws Exception {
        // Generate a random secret key for testing
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String secretBase64 = Base64.getEncoder().encodeToString(key.getEncoded());

        jwtTokenProvider = new JwtTokenProvider(secretBase64);

        // Inject expiration value via reflection since it is set by Spring's @Value in production
        Field expirationField = JwtTokenProvider.class.getDeclaredField("jwtExpirationInMs");
        expirationField.setAccessible(true);
        // 1 hour
        long expirationMs = 1000 * 60 * 60;
        expirationField.setLong(jwtTokenProvider, expirationMs);
    }

    @Test
    void testGenerateTokenAndGetUsername() {
        String username = "tester";

        String token = jwtTokenProvider.generateToken(username);
        assertNotNull(token);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateToken_validToken() {
        String token = jwtTokenProvider.generateToken("validUser");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_expiredToken() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Set expiration to 1 second for testing expiration
        Field expirationField = JwtTokenProvider.class.getDeclaredField("jwtExpirationInMs");
        expirationField.setAccessible(true);
        expirationField.setLong(jwtTokenProvider, 1000); // 1 second

        String token = jwtTokenProvider.generateToken("user");

        // Wait for token to expire
        Thread.sleep(1500);

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_malformedToken() {
        String malformedToken = "this.is.not.a.valid.token";
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    void testValidateToken_nullOrEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void testGetUsernameFromToken_invalidTokenThrows() {
        String invalidToken = "invalid.token.here";

        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }
}
