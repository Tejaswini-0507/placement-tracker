package com.example.placement_tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "positions", indexes = {
        @Index(name = "idx_position_company", columnList = "company_id")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id",nullable = false)
    private Company company;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "batch_applicable")
    private Integer batchApplicable;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ctc")
    private String ctc;

    @Column(name = "bond_period")
    private String bondPeriod;

    @Column(name = "location")
    private String location;

    @Column(name = "total_positions")
    private Integer totalPositions;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Long createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
    }


}
