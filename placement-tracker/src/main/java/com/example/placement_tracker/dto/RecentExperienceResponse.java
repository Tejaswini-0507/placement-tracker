package com.example.placement_tracker.dto;

import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.enums.InterviewRound;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RecentExperienceResponse {
    private UUID id;

    private UUID companyId;

    private String companyName;

    private InterviewRound interviewRound;

    private DifficultyLevel difficultyLevel;

    private InterviewResult result;

    private String topics;

    private Integer upvotes;

    private Integer downvotes;

    private Long createdAt;

}
