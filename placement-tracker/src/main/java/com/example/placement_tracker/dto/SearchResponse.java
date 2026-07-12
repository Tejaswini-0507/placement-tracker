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
public class SearchResponse {
    private UUID id;

    private UUID studentId;

    private String studentName;

    private UUID companyId;

    private String companyName;

    private String interviewRound;

    private String difficultyRating;

    private Integer durationMinutes;

    private Integer totalProblemsAsked;

    private String questionsAsked;

    private String topics;

    private String experienceSummary;

    private String helpfulResources;

    private String result;

    private Long resultReceivedDate;

    private Boolean isPublic;

    private Integer upvotes;

    private Integer downvotes;

    private Long createdAt;

    private Long updatedAt;

    private Float score;



}
