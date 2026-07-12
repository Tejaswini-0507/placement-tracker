package com.example.placement_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AnalyticsResponse {

    private UUID id;
    private String companyName;
    private String interviewRound;
    private String topic;
    private Integer frequencyCount;
    private Double percentage;
    private Double difficultyAvg;
    private Long lastUpdated;

}
