package com.example.placement_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;

    private String studentId;

    private String email;

    private String name;

    private String branch;

    private Integer batch;

    private Long expiresIn;

    private String message;


}
