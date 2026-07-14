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
public class DiscussionMessageResponse {

    private UUID id;
    private UUID threadId;
    private UUID studentId;
    private String studentName;
    private String message;
    private Integer likes;
    private Boolean isEdited;
    private Long editedAt;
    private Long createdAt;
}
