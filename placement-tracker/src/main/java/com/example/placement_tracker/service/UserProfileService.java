package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.UserProfileRequest;
import com.example.placement_tracker.dto.UserProfileResponse;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    @Autowired
    StudentRepository studentRepository;

    //GET MY PROFILE
    public UserProfileResponse getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return entityToResponse(student);
    }

    // GET PROFILE BY ID
    public UserProfileResponse getProfileById(String studentId) {
        Student student = studentRepository.findById(java.util.UUID.fromString(studentId))
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return entityToResponse(student);
    }

    // UPDATE MY PROFILE
    public UserProfileResponse updateMyProfile(UserProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));


        // Update fields
        if (request.getName() != null) student.setName(request.getName());
        if (request.getBranch() != null) student.setBranch(request.getBranch());
        if (request.getBatch() != null) student.setBatch(request.getBatch());
        if (request.getPhoneNumber() != null) student.setPhoneNumber(request.getPhoneNumber());
        if (request.getLinkedinUrl() != null) student.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) student.setGithubUrl(request.getGithubUrl());
        if(request.getBio() != null) student.setBio(request.getBio());


        student = studentRepository.save(student);

        System.out.println("Student updated");

        logger.info("Profile updated for student: {}", email);

        return entityToResponse(student);
    }

    // HELPER: Convert entity to DTO
    private UserProfileResponse entityToResponse(Student student) {
        return UserProfileResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .branch(student.getBranch())
                .batch(student.getBatch())
                .phoneNumber(student.getPhoneNumber())
                .linkedinUrl(student.getLinkedinUrl())
                .githubUrl(student.getGithubUrl())
                .bio(student.getBio())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

}
