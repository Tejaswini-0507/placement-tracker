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
public class UserProfileResponse {

    private UUID id;

    private String name;

    private String email;

    private String branch;

    private Integer batch;

    private String phoneNumber;

    private String linkedinUrl;

    private String githubUrl;

    private String bio;

    private Long createdAt;

    private Long updatedAt;



}
