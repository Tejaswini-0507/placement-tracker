package com.example.placement_tracker.dto;

import com.example.placement_tracker.enums.InterviewRound;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionThreadRequest {

    @NotNull(message = "Company Id is required")
    private UUID companyId;

    @NotNull(message = "Student Id is required")
    private UUID studentId;

    private InterviewRound interviewRound;

    private String topic;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;






}
