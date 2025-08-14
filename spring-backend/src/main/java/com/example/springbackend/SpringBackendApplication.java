package com.example.springbackend;

import com.example.springbackend.model.Feedback;
import com.example.springbackend.repository.FeedbackRepository;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@SpringBootApplication(excludeName = "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration")
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
                                .feedbackText("I used to leave for the bus stop way too early because I didn’t " +
                                        "trust the bus timing boards. Now this app sends me a notification at just the right moment, and" +
                                        " I can enjoy my coffee without rushing. The chatbot is super handy" +
                                        " — I just type “When’s the next 196?” and it answers instantly.")
                                .rating(5)
                                .tagList("schedule")
                                .submittedAt(LocalDateTime.now())
                                .build(),
                        Feedback.builder()
                                .userName(user.getUserName())
                                .userId(user.getId())
                                .feedbackText("Love the real-time updates and route planning. The navigation to " +
                                        "the nearest bus stop is very accurate, and the chatbot actually understands " +
                                        "my questions. I hope they add MRT integration soon so I can plan my train connections as well.")
                                .rating(4)
                                .tagList("performance")
                                .submittedAt(LocalDateTime.now())
                                .build(),
                        Feedback.builder()
                                .userName(user.getUserName())
                                .userId(user.getId())
                                .feedbackText("My commute involves two different buses, and this app finally " +
                                        "makes it easy to coordinate them without guessing. It even tells me which " +
                                        "bus is faster depending on the time of day. Notifications are spot-on — I’ve " +
                                        "never missed a bus since I started using it.")
                                .rating(5)
                                .tagList("performance")
                                .submittedAt(LocalDateTime.now())
                                .build()
                );

                feedbackRepository.saveAll(feedbackList).collectList().block();
            }
        };

    }



}
