package com.example.springbackend.services;

import com.example.springbackend.dto.*;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest authRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        String token = jwtTokenProvider.generateToken(authRequest.getUsername());
        return new AuthResponse(token);
    }

    public String register(AuthRequest authRequest) {
        // check if user already exist
        if (userRepository.findByUserName(authRequest.getUsername()).isPresent()) {
            return "username already in use";
        }

        // set new user and add passwordHash
        User newUser = new User();
        newUser.setUserName(authRequest.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(authRequest.getPassword()));
        userRepository.save(newUser);

        return "registration successful";
    }
}