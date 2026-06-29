package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.TopicFrequencyAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicFrequencyAnalyticsRepository extends JpaRepository<TopicFrequencyAnalytics , String> {
}
