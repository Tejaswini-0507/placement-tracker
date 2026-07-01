package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.InterviewRoundConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewRoundConfigRepository extends JpaRepository<InterviewRoundConfig, UUID> {

    List<InterviewRoundConfig> findByPosition_IdOrderByRoundOrderAsc(UUID positionId);
    InterviewRoundConfig findByPosition_IdAndRoundOrder(UUID positionId, Integer roundOrder);
}
