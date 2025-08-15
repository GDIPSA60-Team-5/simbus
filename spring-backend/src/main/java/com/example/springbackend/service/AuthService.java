package com.example.springbackend.service;

import com.example.springbackend.dto.MessageResponse;
import com.example.springbackend.dto.request.LoginRequest;
import com.example.springbackend.dto.request.RegisterRequest;
import com.example.springbackend.dto.response.AuthResponse;
import com.example.springbackend.security.UserDetailsAuthenticationManager;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

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

    public Mono<AuthResponse> login(LoginRequest authRequest) {
        return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password()))
                .map(auth -> {
                    String token = jwtTokenProvider.generateToken(authRequest.username());
                    return new AuthResponse(token);
                });
    }

    public Mono<AuthResponse> adminLogin(LoginRequest authRequest) {
        return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password()))
                .flatMap(auth -> {
                    return userRepository.findByUserName(authRequest.username())
                            .filter(user -> "admin".equals(user.getUserType()))
                            .switchIfEmpty(Mono.error(new BadCredentialsException("Admin access required")))
                            .map(user -> {
                                String token = jwtTokenProvider.generateToken(authRequest.username());
                                return new AuthResponse(token);
                            });
                });
    }

    public Mono<MessageResponse> register(RegisterRequest request) {
        // Check if username already exists
        Mono<Boolean> usernameExists = userRepository.findByUserName(request.username())
                .hasElement();

        // Check if email already exists
        Mono<Boolean> emailExists = userRepository.findByEmail(request.email())
                .hasElement();

        return Mono.zip(usernameExists, emailExists)
                .flatMap(tuple -> {
                    boolean usernameTaken = tuple.getT1();
                    boolean emailTaken = tuple.getT2();

                    if (usernameTaken && emailTaken) {
                        return Mono.<MessageResponse>error(new IllegalArgumentException("Username and email are already in use"));
                    } else if (usernameTaken) {
                        return Mono.<MessageResponse>error(new IllegalArgumentException("Username already in use"));
                    } else if (emailTaken) {
                        return Mono.<MessageResponse>error(new IllegalArgumentException("Email already in use"));
                    } else {
                        // Both username and email are available, create user
                        User user = User.builder()
                                .userName(request.username())
                                .userType("user")
                                .email(request.email())
                                .passwordHash(passwordEncoder.encode(request.password()))
                                .createdAt(new Date())
                                .build();

                        return userRepository.save(user)
                                .then(Mono.just(new MessageResponse("Registration successful")));
                    }
                });
    }

}
