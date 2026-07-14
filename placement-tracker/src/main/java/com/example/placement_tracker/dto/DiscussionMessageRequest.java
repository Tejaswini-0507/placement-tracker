package com.example.placement_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionMessageRequest {

    @NotNull(message = "Thread Id is required")
    private UUID threadId;

    @NotBlank(message = "Message is required")
    private String message;

}
