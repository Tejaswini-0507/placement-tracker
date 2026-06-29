package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.InterviewExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewExperienceRepository extends JpaRepository<InterviewExperienceRepository ,String> {

    List<InterviewExperience> findByCompanyId(String companyId);
    List<InterviewExperience> findByStudentId(String studentId);
    List<InterviewExperience> findByCompanyIdAndInterviewRound(String companyId, String interviewRound);
}
