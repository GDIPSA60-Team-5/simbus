package com.example.springbackend.service;

import com.example.springbackend.model.User;
import com.example.springbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Update the FCM token for a user by username123
     */
    public Mono<User> updateFcmToken(String username, String fcmToken) {
        return userRepository.findByUserName(username)
                .flatMap(user -> {
                    user.setFcmToken(fcmToken);
                    return userRepository.save(user);
                });
    }
}
