package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.PrepRoadmap;
import com.example.placement_tracker.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrepRoadmapRepository extends JpaRepository<PrepRoadmap, UUID> {

    List<PrepRoadmap> findByPosition_Id(UUID positionId);
    List<PrepRoadmap> findByPosition_IdAndDifficultyLevel(UUID positionId, String difficultyLevel);

}
