package com.example.springbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsAuthenticationManagerTest {

    private ReactiveUserDetailsServiceImpl userDetailsService;
    private PasswordEncoder passwordEncoder;
    private UserDetailsAuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        userDetailsService = mock(ReactiveUserDetailsServiceImpl.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = new UserDetailsAuthenticationManager(userDetailsService, passwordEncoder);
    }

    @Test
    void authenticate_successful() {
        String username = "tester";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        // Mock UserDetails with encoded password and authorities
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(encodedPassword)
                .authorities("ROLE_USER")
                .build();

        Authentication authRequest = new UsernamePasswordAuthenticationToken(username, rawPassword);

        when(userDetailsService.findByUsername(username)).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        StepVerifier.create(authenticationManager.authenticate(authRequest))
                .assertNext(auth -> {
                    assertNotNull(auth);
                    assertEquals(userDetails, auth.getPrincipal());
                    assertNull(auth.getCredentials());
                    assertTrue(auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
                })
                .verifyComplete();

        verify(userDetailsService).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void authenticate_badCredentials() {
        String username = "tester";
        String rawPassword = "wrong password";
        String encodedPassword = "encodedPassword";

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(encodedPassword)
                .authorities("ROLE_USER")
                .build();

        Authentication authRequest = new UsernamePasswordAuthenticationToken(username, rawPassword);

        when(userDetailsService.findByUsername(username)).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        StepVerifier.create(authenticationManager.authenticate(authRequest))
                .expectErrorSatisfies(ex -> {
                    assertInstanceOf(BadCredentialsException.class, ex);
                    assertEquals("Invalid Credentials", ex.getMessage());
                })
                .verify();

        verify(userDetailsService).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void authenticate_userNotFound() {
        String username = "unknown";
        String rawPassword = "password123";

        Authentication authRequest = new UsernamePasswordAuthenticationToken(username, rawPassword);

        when(userDetailsService.findByUsername(username)).thenReturn(Mono.empty());

        StepVerifier.create(authenticationManager.authenticate(authRequest))
                .expectErrorSatisfies(ex -> {
                    assertInstanceOf(BadCredentialsException.class, ex);
                    assertEquals("User not found", ex.getMessage());
                })
                .verify();

        verify(userDetailsService).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
