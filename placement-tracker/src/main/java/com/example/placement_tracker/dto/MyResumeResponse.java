package com.example.placement_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyResumeResponse {
    private Integer totalVersions;
    private List<ResumeDetailResponse> resumes;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResumeDetailResponse {
        private Integer versionNumber;
        private String fileUrl;
        private Long fileSizeBytes;
        private String notes;
        private Long createdAt;
        private Boolean isCurrent;
    }
}
