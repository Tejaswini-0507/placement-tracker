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
@Table(name = "interview_round_configs",indexes = {
        @Index(name =  "idx_irc_position",columnList = "position_id"),
        @Index(name =  "idx_irc_position_order",columnList = "position_id, round_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class InterviewRoundConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "round_number",nullable = false)
    private Integer roundOrder;

    @Column(name = "round_name",nullable = false)
    private String roundName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_topics",columnDefinition = "jsonb")
    private JsonNode expectedTopics;

    @Column(name = "is_elimination_round")
    private Boolean isEliminationRound;

    @Column(name = "created_at", nullable = false,updatable = false)
    private Long createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
        if(isEliminationRound == null) isEliminationRound = true;
    }

}
