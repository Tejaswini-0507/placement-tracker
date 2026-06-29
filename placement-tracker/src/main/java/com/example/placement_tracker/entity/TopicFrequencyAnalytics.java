package com.example.placement_tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "topic_frequency_analytics", indexes = {
        @Index(name = "idx_analytics_company_round", columnList = "company_id, interview_round"),
        @Index(name = "idx_analytics_frequency",columnList = "frequency_count DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicFrequencyAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id",nullable = false)
    private Company company;

    @Column(name = "interview_round")
    private String interviewRound;

    @Column(name = "topic",nullable = false)
    private String topic;

    @Column(name = "frequency_count")
    private Integer frequencyCount;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "difficulty_avg")
    private Double difficultyAvg;

    @Column(name = "last_updated",nullable = false)
    private Long lastUpdated;

    @PrePersist
    public void onCreate(){
        lastUpdated = System.currentTimeMillis();
    }

    @PreUpdate
    public void onUpdate(){
        lastUpdated = System.currentTimeMillis();
    }
}
