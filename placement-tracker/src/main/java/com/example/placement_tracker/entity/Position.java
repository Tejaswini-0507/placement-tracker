package com.example.placement_tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "positions",
        indexes = {
            @Index(name = "idx_position_company", columnList = "company_id")},
        uniqueConstraints = {
            @UniqueConstraint(name ="uk_company_title",columnNames = {"company_id","title"})
        }
)
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

    @Column(name = "location",nullable = false)
    private String location;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Long createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
    }


}
