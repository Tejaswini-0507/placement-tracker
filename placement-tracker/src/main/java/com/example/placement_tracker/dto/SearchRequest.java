package com.example.placement_tracker.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String query;

    private UUID companyId;

    private String interviewRound;

    private String difficultyRating;

    private List<String> topics;

    private String result;

    private Boolean isPublic;

    @Min(value = 0, message = "Page must be o or greater")
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    private Integer size = 20;

    private String sortBy = "createdAt";

    private String sortOrder = "desc";
}
