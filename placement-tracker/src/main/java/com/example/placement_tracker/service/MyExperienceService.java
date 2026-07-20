package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.MyExperienceResponse;
import com.example.placement_tracker.entity.InterviewExperience;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.repository.InterviewExperienceRepository;
import com.example.placement_tracker.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MyExperienceService {

    private static final Logger logger = LoggerFactory.getLogger(MyExperienceService.class);

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    InterviewExperienceRepository experienceRepository;

    @Transactional
    public MyExperienceResponse getMyExperiences(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() ->  new IllegalArgumentException("Student not found"));


        List<InterviewExperience> experiences = experienceRepository.findByStudent_Id(student.getId());

        Map<String , List<MyExperienceResponse.ExperienceDetailResponse>> byCompany = experiences.stream()
                .collect(Collectors.groupingBy(
                        exp-> exp.getCompany().getName(),
                        Collectors.mapping(this :: toDetailResponse, Collectors.toList())
                ));

        long passedCount = experiences.stream()
                .filter(exp -> "PASSED".equals(exp.getResult()))
                .count();

        long failedCount = experiences.stream()
                .filter(exp -> "FAILED".equals(exp.getResult()))
                .count();

        double avgDifficulty = experiences.stream()
                .mapToInt(exp -> convertDifficultyToNumeric(exp.getDifficultyRating()))
                .average()
                .orElse(0.0);

        return MyExperienceResponse.builder()
                .totalExperiences(experiences.size())
                .experiencesByCompany(byCompany)
                .passedCount((int) passedCount)
                .failedCount((int) failedCount)
                .averageDifficulty(Math.round(avgDifficulty * 100.0) / 100.0)
                .build();
    }

    // Helper
    private MyExperienceResponse.ExperienceDetailResponse toDetailResponse(InterviewExperience exp) {
        return MyExperienceResponse.ExperienceDetailResponse.builder()
                .companyName(exp.getCompany().getName())
                .interviewRound(exp.getInterviewRound().toString())
                .difficultyLevel(exp.getDifficultyRating().toString())
                .result(exp.getResult().toString())
                .topics(exp.getTopics().toString())
                .upvotes(exp.getUpvotes())
                .downvotes(exp.getDownvotes())
                .createdAt(exp.getCreatedAt())
                .build();
    }

    private Integer convertDifficultyToNumeric(com.example.placement_tracker.enums.DifficultyLevel level) {
        return switch(level) {
            case EASY -> 3;
            case MEDIUM -> 5;
            case HARD -> 8;
            case EXPERT -> 10;
        };
    }

}
