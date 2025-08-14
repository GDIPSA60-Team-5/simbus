package com.example.springbackend.controller;

import com.example.springbackend.dto.ChangePasswordRequest;
import com.example.springbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public Mono<ResponseEntity<String>> changePassword(@RequestBody ChangePasswordRequest req,
                                                       Principal principal) {
        // (1) Old/new password must not be empty
        if (req == null ||
                req.getCurrentPassword() == null || req.getCurrentPassword().trim().isEmpty() ||
                req.getNewPassword() == null || req.getNewPassword().trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("Parameters cannot be empty"));
        }

        final String oldPwd = req.getCurrentPassword().trim();
        final String newPwd = req.getNewPassword().trim();

        // (2) New password length must be at least 8
        if (newPwd.length() < 8) {
            return Mono.just(ResponseEntity.badRequest().body("Password must be at least 8 characters"));
        }

        // (3) New password must be different from old password
        if (oldPwd.equals(newPwd)) {
            return Mono.just(ResponseEntity.badRequest().body("New password must be different from the current password"));
        }

        // (4) Verify old password and update to the new password
        return userRepository.findByUserName(principal.getName())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(oldPwd, user.getPasswordHash())) {
                        // Old password is incorrect
                        return Mono.just(ResponseEntity.badRequest().body("Current password is incorrect"));
                    }
                    user.setPasswordHash(passwordEncoder.encode(newPwd));
                    return userRepository.save(user)
                            .thenReturn(ResponseEntity.ok("Password changed successfully"));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("User not found")));
    }
}

