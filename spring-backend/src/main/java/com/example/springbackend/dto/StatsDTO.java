package com.example.springbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsDTO {
    private long userCount;
    private long userCountRecently;
    private long feedbackCount;
    private long feedbackCountRecently;
    private long botRequestCount;
    private long botSuccessCount;
    private double botSuccessRate;
    private double avgResponseTimeMs;
    private double maxResponseTimeMs;
    private double minResponseTimeMs;
}
