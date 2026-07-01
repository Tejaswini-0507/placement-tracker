package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.InterviewExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewExperienceRepository extends JpaRepository<InterviewExperience ,UUID> {

    List<InterviewExperience> findByCompany_Id(UUID companyId);
    List<InterviewExperience> findByStudent_Id(UUID studentId);
    List<InterviewExperience> findByCompany_IdAndInterviewRound(UUID companyId, String interviewRound);
}
