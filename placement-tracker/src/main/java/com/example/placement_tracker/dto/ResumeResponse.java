package com.example.placement_tracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private UUID id;
    private UUID studentId;
    private Integer versionNumber;
    private String fileUrl;
    private Long fileSizeBytes;
    private String notes;
    private String usedForCompanies;
}
