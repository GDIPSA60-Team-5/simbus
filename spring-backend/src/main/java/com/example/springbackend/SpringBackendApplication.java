package com.example.springbackend;

import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SpringBackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpringBackendApplication.class, args);

    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            userRepository.findByUserName("user")
                    .switchIfEmpty(
                            userRepository.save(
                                    User.builder()
                                            .userName("user")
                                            .userType("admin")
                                            .passwordHash(passwordEncoder.encode("password"))
                                            .build()))
                    .block();
        };
    }

}
