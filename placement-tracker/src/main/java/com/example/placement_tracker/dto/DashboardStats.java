package com.example.placement_tracker.dto;

import com.example.placement_tracker.entity.ResumeVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private Integer totalApplications;

    private Integer offersCount;

    private Integer interviewsCount;

    private Integer rejectedCount;

    private Integer appliedCount;

    private Integer totalExperiences;

    private Integer totalResumeVersions;

    private List<RecentApplicationResponse> recentApplications;
    private List<RecentExperienceResponse> recentExperiences;
    private List<ResumeVersionResponse> resumeVersions;

    private Double applicationSuccessRate;
    private Long lastUpdated;
}
