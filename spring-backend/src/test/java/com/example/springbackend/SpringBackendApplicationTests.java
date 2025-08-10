package com.example.springbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SpringBackendApplicationTests.class)
@ActiveProfiles("test")
class SpringBackendApplicationTests {
    @Test
    void contextLoads() {
    }
}
