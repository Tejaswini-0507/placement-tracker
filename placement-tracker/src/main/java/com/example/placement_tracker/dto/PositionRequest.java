package com.example.placement_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PositionRequest {

    @NotBlank(message = "Position title is required")
    private String title;

    @NotBlank(message = "Loaction is required")
    private String location;

}
