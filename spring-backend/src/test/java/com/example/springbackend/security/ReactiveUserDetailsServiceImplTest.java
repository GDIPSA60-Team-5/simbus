package com.example.springbackend.security;

import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReactiveUserDetailsServiceImplTest {

    private UserRepository userRepository;
    private ReactiveUserDetailsServiceImpl service;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        service = new ReactiveUserDetailsServiceImpl(userRepository);
    }

    @Test
    void findByUsername_UserFound_ReturnsUserDetails() {
        User user = User.builder()
                .userName("testuser")
                .passwordHash("hashedpassword123")
                .build();

        when(userRepository.findByUserName("testuser")).thenReturn(Mono.just(user));

        StepVerifier.create(service.findByUsername("testuser"))
                .assertNext(userDetails -> {
                    assertEquals("testuser", userDetails.getUsername());
                    assertEquals("hashedpassword123", userDetails.getPassword());
                    assertTrue(userDetails.getAuthorities().isEmpty());
                })
                .verifyComplete();

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void findByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepository.findByUserName("nonexistent")).thenReturn(Mono.empty());

        StepVerifier.create(service.findByUsername("nonexistent"))
                .expectErrorMatches(throwable ->
                        throwable instanceof UsernameNotFoundException &&
                                throwable.getMessage().equals("User not found"))
                .verify();

        verify(userRepository).findByUserName("nonexistent");
    }
}
