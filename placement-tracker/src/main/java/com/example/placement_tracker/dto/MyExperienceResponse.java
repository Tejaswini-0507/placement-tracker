package com.example.placement_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyExperienceResponse {
    private Integer totalExperiences;

    // Grouped by company
    private Map<String, List<ExperienceDetailResponse>> experiencesByCompany;

    // Summary
    private Integer passedCount;
    private Integer failedCount;
    private Double averageDifficulty;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExperienceDetailResponse {
        private String companyName;
        private String interviewRound;
        private String difficultyLevel;
        private String result;
        private String topics;
        private Integer upvotes;
        private Integer downvotes;
        private Long createdAt;
    }
}
