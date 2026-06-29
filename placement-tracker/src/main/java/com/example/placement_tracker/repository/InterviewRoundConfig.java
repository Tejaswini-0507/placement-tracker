package com.example.placement_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRoundConfig extends JpaRepository<com.example.placement_tracker.entity.InterviewRoundConfig, String> {

    List<InterviewRoundConfig> findByPositionIdOrderByRoundOrderAsc(String positionId);
    InterviewRoundConfig findByPositionIdAndRoundOrder(String positionId, Integer roundOrder);
}
