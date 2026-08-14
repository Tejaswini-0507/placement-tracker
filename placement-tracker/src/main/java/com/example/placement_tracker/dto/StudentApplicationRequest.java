package com.example.placement_tracker.dto;

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
public class StudentApplicationRequest {

    @NotNull(message = "CompanyId is required")
    private UUID companyId;

//    @NotBlank(message = "Position title is required")
//    private String positionTitle;

//    @NotBlank(message = "Location is required")
//    private String location;

    @NotBlank(message = "status is required")
    private String status;

    @NotNull(message = "status updated at is required")
    private Long statusUpdatedAt;

    private Long oaScheduledDate;

    private Long oaCompletedDate;

    private Long interviewScheduledDate;

    private Long interviewCompletedDate;

    private Long resultReceivedDate;

    private Boolean offerAccepted;

    private String notes;

}
