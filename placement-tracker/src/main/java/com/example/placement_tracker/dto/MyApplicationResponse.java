package com.example.placement_tracker.dto;

import com.example.placement_tracker.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyApplicationResponse {

    private Integer totalApplications;

    // Grouped by status
    private List<ApplicationDetailResponse> appliedApplications;
    private List<ApplicationDetailResponse> interviewApplications;
    private List<ApplicationDetailResponse> offerApplications;
    private List<ApplicationDetailResponse> rejectedApplications;

    // Summary by status
    private Map<String, Integer> statusCount;
    private Double successRate;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationDetailResponse {
        private String companyName;
        private String position;
        private ApplicationStatus status;
        private Long createdAt;
        private Long statusUpdatedAt;
        private String notes;
    }

}
