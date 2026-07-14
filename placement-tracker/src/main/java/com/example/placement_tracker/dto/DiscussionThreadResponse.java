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
public class DiscussionThreadResponse {

    private UUID id;
    private UUID companyId;
    private String companyName;
    private UUID studentId;
    private String studentName;
    private UUID createdByStudentId;
    private String createdByStudentName;
    private String interviewRound;
    private String topic;
    private String title;
    private String description;
    private Integer messageCount;
    private Boolean pinned;
    private Long lastActivity;
    private Long createdAt;


}
