package com.example.springbackend;

import com.example.springbackend.model.Feedback;
import com.example.springbackend.repository.FeedbackRepository;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@SpringBootApplication(excludeName = "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration")
@EnableScheduling
public class SpringBackendApplication {
	
    public static void main(String[] args) {

        SpringApplication.run(SpringBackendApplication.class, args);

    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder, FeedbackRepository feedbackRepository) {
        return args -> {
            User user = userRepository.findByUserName("user")
                    .switchIfEmpty(
                            userRepository.save(
                                    User.builder()
                                            .userName("user")
                                            .userType("admin")
                                            .passwordHash(passwordEncoder.encode("password"))
                                            .createdAt(new Date())
                                            .build()
                            )
                    )
                    .block();
            Long count = feedbackRepository.count().block();
            if (count != null && count == 0L) {
                assert user != null;
                List<Feedback> feedbackList = Arrays.asList(
                        Feedback.builder()
                                .userName(user.getUserName())
                                .userId(user.getId())
                                .feedbackText("wow")
                                .rating(5)
                                .tagList("chatbot")
                                .submittedAt(LocalDateTime.now())
                                .build(),
                        Feedback.builder()
                                .userName(user.getUserName())
                                .userId(user.getId())
                                .feedbackText("nice")
                                .rating(4)
                                .tagList("performance")
                                .submittedAt(LocalDateTime.now())
                                .build(),
                        Feedback.builder()
                                .userName(user.getUserName())
                                .userId(user.getId())
                                .feedbackText("lee")
                                .rating(3)
                                .tagList("direction")
                                .submittedAt(LocalDateTime.now())
                                .build()
                );

                feedbackRepository.saveAll(feedbackList).collectList().block();
            }
        };

    }



}
