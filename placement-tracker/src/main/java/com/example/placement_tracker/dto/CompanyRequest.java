package com.example.placement_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    private String name;

    private String logoUrl;

    private String website;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Head quarters is required")
    private String headQuarters;

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotBlank(message = "Hiring for is required")
    private String hiringFor;

    private String packagesOffered;

    @Positive(message = "Average Difficulty must be positive")
    private BigDecimal averageDifficulty;

    @Positive(message = "Total applicants must be positive")
    private Integer totalApplicants;

    @Positive(message = "Total selected must be positive")
    private Integer totalSelected;


}
