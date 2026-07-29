package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.InterviewRoundConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRoundConfigRepository extends JpaRepository<InterviewRoundConfig, UUID> {

    List<InterviewRoundConfig> findByCompany_IdOrderByRoundNumberAsc(UUID companyId);
    InterviewRoundConfig findByCompany_IdAndRoundNumber(UUID positionId, Integer roundNumber);
    Optional<InterviewRoundConfig> findByCompanyAndRoundNameIgnoreCase(Company company, String roundName);
}
