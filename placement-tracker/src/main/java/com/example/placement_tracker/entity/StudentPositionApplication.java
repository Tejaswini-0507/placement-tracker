package com.example.placement_tracker.entity;

import com.example.placement_tracker.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_position_applications",indexes = {
        @Index(name = "idx_spa_position_id", columnList = "position_id"),
        @Index(name = "idx_spa_status", columnList = "status"),
        @Index(name = "idx_spa_student_id",columnList = "student_id"),
        @Index(name = "idx_spa_student_position", columnList = "student_id, position_id")
},uniqueConstraints = {
        @UniqueConstraint(name =  "student_position_applications_student_id_position_id_key", columnNames = {"student_id, position_id"})
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentPositionApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id",nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id",nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private ApplicationStatus status;

    @Column(name = "current_round_number")
    private Integer currentRoundNumber;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Long createdAt;

    @Column(name = "updated_at",nullable = false)
    private Long updatedAt;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
        if(currentRoundNumber == null) currentRoundNumber = 1;
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = System.currentTimeMillis();
    }
}
