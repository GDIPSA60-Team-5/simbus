package com.example.springbackend.service;

import com.example.springbackend.security.UserDetailsAuthenticationManager;
import com.example.springbackend.dto.*;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserDetailsAuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(@Qualifier("userDetailsAuthenticationManager") UserDetailsAuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<AuthResponse> login(AuthRequest authRequest) {
        return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password()))
                .map(auth -> {
                    String token = jwtTokenProvider.generateToken(authRequest.username());
                    return new AuthResponse(token);
                });
    }

    public Mono<String> register(AuthRequest authRequest) {
        return userRepository.findByUserName(authRequest.username())
                .flatMap(existingUser -> Mono.<String>error(new IllegalArgumentException("Username already in use")))
                .switchIfEmpty(
                        Mono.defer(() ->
                                userRepository.save(
                                        User.builder()
                                                .userName(authRequest.username())
                                                .passwordHash(passwordEncoder.encode(authRequest.password()))
                                                .build()
                                ).thenReturn("registration successful")
                        )
                );
    }
}
