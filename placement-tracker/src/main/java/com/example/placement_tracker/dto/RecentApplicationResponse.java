package com.example.placement_tracker.dto;

import com.example.placement_tracker.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentApplicationResponse {

    private UUID id;

    private UUID companyId;

    private String companyName;

    private ApplicationStatus status;

    private Long createdAt;

    private Long statusUpdatedAt;

    private String notes;
}
