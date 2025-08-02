package com.example.springbackend;

import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootApplication
public class SpringBackendApplication {
	
    public static void main(String[] args) {

        SpringApplication.run(SpringBackendApplication.class, args);

    }


    //input userName(user) and passwordHash(password) into database when run application
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUserName("user").isEmpty()) {
                User user = new User();
                user.setUserName("user");
                user.setUserType("admin");
                user.setPasswordHash(passwordEncoder.encode("password"));
                userRepository.save(user);
            }
        };
    }


}
