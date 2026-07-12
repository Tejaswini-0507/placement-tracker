package com.example.placement_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadRequest {
    private MultipartFile file;

    @NotNull(message = "Version number is required")
    private Integer versionNumber;

    private String notes;

    private String usedForCompanies;

}
