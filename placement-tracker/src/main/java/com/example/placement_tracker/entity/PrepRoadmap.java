package com.example.placement_tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "prep_roadmaps",indexes = {
        @Index(name = "idx_roadmap_company",columnList = "company_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PrepRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id",nullable = false)
    private Position position;

    @Column(name = "created_by_admin")
    private Boolean createdByAdmin;

    @Column(nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "topics",columnDefinition = "jsonb")
    private JsonNode topics;

    @Column(name = "resource_links",columnDefinition = "jsonb")
    private JsonNode resourceLinks;

    @Column(name = "created_at",nullable = false, updatable = false)
    private Long createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
        if(createdByAdmin == null)createdByAdmin = false;
    }

}
