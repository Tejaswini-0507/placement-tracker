package com.example.placement_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewExperienceRequest {

    @NotNull(message = "Company Id is required")
    private UUID companyId;

    @NotNull (message = "Position Id is required")
    private UUID positionId;

    @NotBlank(message = "Round name is required")
    private String roundName;

    @NotNull(message = "Round number is required")
    private Integer roundNumber;

    @NotNull(message = "Date experienced is required")
    private Long dateExperienced;

    @NotBlank(message = "Difficulty Rating is required")
    private String difficultyRating;

    private Integer durationMinutes;

    private Integer totalProblemsAsked;

    @NotBlank(message = "Questions asked is required")
    private String questionsAsked;

    private String questionsJson;

    private String topics;

    @NotBlank(message = "Experience Summary is required")
    private String experienceSummary;

    private String helpfulResources;
    private String interviewerFeedback;

    @NotBlank(message = "Result is Required")
    private String result;

    private Long resultReceivedDate;

    private Boolean isPublic;



}
