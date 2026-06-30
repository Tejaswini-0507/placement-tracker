package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.TopicFrequencyAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicFrequencyAnalyticsRepository extends JpaRepository<TopicFrequencyAnalytics , String> {

    List<TopicFrequencyAnalytics> findByCompany_Id(String companyId);
    List<TopicFrequencyAnalytics> findByCompany_IdAndInterviewRound(String companyId, String interviewRound);
    List<TopicFrequencyAnalytics> findByCompany_IdOrderByFrequencyCountDesc(String companyId);
}
