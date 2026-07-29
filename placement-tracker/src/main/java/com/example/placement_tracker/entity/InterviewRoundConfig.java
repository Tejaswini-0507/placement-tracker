package com.example.placement_tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//import org.hibernate.annotations.EmbeddedColumnNaming;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
//import tools.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonNode;  // ✅ CORRECT

import java.util.UUID;

@Entity
@Table(name = "interview_round_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class InterviewRoundConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "round_name",nullable = false)
    private String roundName;

    @Column(name = "round_number",nullable = false)
    private Integer roundNumber;


    @Column(name = "created_at", nullable = false,updatable = false)
    private Long createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
    }

}
