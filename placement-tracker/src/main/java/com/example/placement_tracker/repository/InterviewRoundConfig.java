package com.example.placement_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRoundConfig extends JpaRepository<InterviewRoundConfig, String> {

    List<InterviewRoundConfig> findByPosition_IdOrderByRoundOrderAsc(String positionId);
    InterviewRoundConfig findByPosition_IdAndRoundOrder(String positionId, Integer roundOrder);
}
