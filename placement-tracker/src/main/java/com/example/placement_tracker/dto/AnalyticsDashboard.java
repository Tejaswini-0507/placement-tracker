package com.example.placement_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsDashboard {
    private UUID companyId;
    private String companyName;
    private Integer totalExperiences;
    private Double averageDifficulty;
    private Integer passRate;

    private Map<String, List<AnalyticsResponse>> topicsByRound;

    private List<AnalyticsResponse> topTopics;

    private Map<String, Double> difficultyByRound;

    private Long lastUpdated;

}
