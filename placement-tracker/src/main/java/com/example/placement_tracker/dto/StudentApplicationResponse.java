package com.example.placement_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentApplicationResponse {

    private UUID id;

    private UUID studentId;

    private String studentName;

    private UUID companyId;

    private String companyName;

    private UUID positionId;

    private String positionTitle;

    private String status;

    private Long statusUpdatedAt;

    private Long oaScheduledDate;

    private Long oaCompletedDate;

    private Long interviewScheduledDate;

    private Long interviewCompletedDate;

    private Long resultReceivedDate;

    private Boolean offerAccepted;

    private String notes;

    private Long createdAt;

    private Long updatedAt;
}
