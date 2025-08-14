package com.example.springbackend.service;

import com.example.springbackend.dto.request.AuthRequest;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.JwtTokenProvider;
import com.example.springbackend.security.UserDetailsAuthenticationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserDetailsAuthenticationManager authenticationManager;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    AuthService authService;

    @BeforeEach
    void setup() {
        authService = new AuthService(userRepository, jwtTokenProvider, passwordEncoder, authenticationManager);
    }

    @Test
    void login_success() {
        AuthRequest request = new AuthRequest("user1", "password1");
        Authentication authMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.just(authMock));
        when(jwtTokenProvider.generateToken(request.username()))
                .thenReturn("jwt-token-123");

        StepVerifier.create(authService.login(request))
                .expectNextMatches(resp -> resp.getToken().equals("jwt-token-123"))
                .verifyComplete();

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtTokenProvider, times(1)).generateToken(request.username());
    }

    @Test
    void register_usernameAlreadyExists() {
        AuthRequest request = new AuthRequest("existingUser", "password");

        when(userRepository.findByUserName(request.username()))
                .thenReturn(Mono.just(User.builder().userName(request.username()).build()));

        StepVerifier.create(authService.register(request))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("Username already in use"))
                .verify();

        verify(userRepository, times(1)).findByUserName(request.username());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_successfulRegistration() {
        AuthRequest request = new AuthRequest("newUser", "password");
        String encodedPassword = "encoded-password";

        when(userRepository.findByUserName(request.username()))
                .thenReturn(Mono.empty());
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(authService.register(request))
                .expectNext("registration successful")
                .verifyComplete();

        verify(userRepository, times(1)).findByUserName(request.username());
        verify(passwordEncoder, times(1)).encode(request.password());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
