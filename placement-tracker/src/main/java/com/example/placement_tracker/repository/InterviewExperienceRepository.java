package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.InterviewExperience;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewExperienceRepository extends JpaRepository<InterviewExperience ,UUID> {

    @EntityGraph(attributePaths = {"student","company"})
    List<InterviewExperience> findByStudent_Id(UUID studentId);

    @EntityGraph(attributePaths = {"company"})
    List<InterviewExperience> findByCompany_Id(UUID companyId);

    @EntityGraph(attributePaths = {"student","company"})
    List<InterviewExperience> findByCompany_IdAndInterviewRoundConfig_Id(UUID companyId, UUID interviewRoundConfigId);
}
