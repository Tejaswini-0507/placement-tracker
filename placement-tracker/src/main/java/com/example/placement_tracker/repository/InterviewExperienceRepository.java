package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.InterviewExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewExperienceRepository extends JpaRepository<InterviewExperience ,String> {

    List<InterviewExperience> findByCompany_Id(String companyId);
    List<InterviewExperience> findByStudent_Id(String studentId);
    List<InterviewExperience> findByCompany_IdAndInterviewRound(String companyId, String interviewRound);
}
