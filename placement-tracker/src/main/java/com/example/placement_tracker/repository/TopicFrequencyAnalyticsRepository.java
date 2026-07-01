package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.TopicFrequencyAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TopicFrequencyAnalyticsRepository extends JpaRepository<TopicFrequencyAnalytics , UUID> {

    List<TopicFrequencyAnalytics> findByCompany_Id(UUID companyId);
    List<TopicFrequencyAnalytics> findByCompany_IdAndInterviewRound(UUID companyId, String interviewRound);
    List<TopicFrequencyAnalytics> findByCompany_IdOrderByFrequencyCountDesc(UUID companyId);
}
