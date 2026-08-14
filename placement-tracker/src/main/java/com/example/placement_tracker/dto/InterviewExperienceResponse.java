package com.example.placement_tracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterviewExperienceResponse {

    private UUID id;

    private UUID studentId;

    private String studentName;

    private UUID companyId;

    private String companyName;

    private UUID positionId;

    private String positionName;

    private UUID interviewRoundConfigId;

    private String interviewRoundName;

    private Integer interviewRoundNumber;

    private Long dateExperienced;

    private String difficultyRating;

    private Integer durationMinutes;

    private Integer totalProblemsAsked;

    private String questionsAsked;

    private JsonNode questionsJson;

    private JsonNode topics;

    private String experienceSummary;

    private String helpfulResources;

    private String interviewerFeedback;

    private String result;

    private Long resultReceivedDate;

    private Boolean isPublic;

    private Integer upvotes;

    private Integer downvotes;

    private Long createdAt;

    private Long updatedAt;



}
