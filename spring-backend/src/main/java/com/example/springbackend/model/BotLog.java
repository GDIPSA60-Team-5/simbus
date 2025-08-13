package com.example.springbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;


@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotLog {
    @Id
    private Long id;
    private String userId;
    private Instant requestTime;
    private Instant responseTime;
    private String userInput;
    private String responseType;

    @Builder.Default
    private boolean success=false;

}
