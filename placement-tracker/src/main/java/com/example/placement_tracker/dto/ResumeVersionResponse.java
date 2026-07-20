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
public class ResumeVersionResponse {

    private UUID id;
    private Integer versionNumber;
    private String fileUrl;
    private Long fileSizeBytes;
    private String notes;
    private Long createdAt;
    private Boolean isCurrent;

}
