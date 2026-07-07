package com.example.placement_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponse {
    private UUID id;

    private String name;

    private String logoUrl;

    private String website;

    private String description;

    private String headQuarters;

    private String industry;

    private String hiringFor;

    private String packagesOffered;

    private BigDecimal averageDifficulty;

    private Integer totalApplicants;

    private Integer totalSelected;

    private Long createdAt;

}
