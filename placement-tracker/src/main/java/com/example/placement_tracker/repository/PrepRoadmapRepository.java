package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.PrepRoadmap;
import com.example.placement_tracker.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrepRoadmapRepository extends JpaRepository<PrepRoadmap, String> {

    List<PrepRoadmap> findByPosition_Id(String positionId);
    List<PrepRoadmap> findByPosition_IdAndDifficultyLevel(String positionId, String difficultyLevel);

}
