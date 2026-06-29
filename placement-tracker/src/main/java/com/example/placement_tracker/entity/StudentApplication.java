package com.example.placement_tracker.entity;

import com.example.placement_tracker.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_applications",indexes = {
        @Index(name = "idx_app_company_id",columnList = "company_id"),
        @Index(name = "idx_app_company_status", columnList = "company_id,status"),
        @Index(name = "idx_app_student_id" , columnList = "student_id"),
        @Index(name = "idx_app_student_status",columnList = "student_id,status")
},uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id","company_id"})
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id",nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id",nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "status_updated_at",nullable = false)
    private Long statusUpdatedAt;

    @Column(name = "oa_scheduled_date")
    private Long oaScheduledDate;

    @Column(name = "oa_completed_date")
    private Long oaCompletedDate;

    @Column(name = "interview_scheduled_date")
    private Long interviewScheduledDate;

    @Column(name = "interview_completed_date")
    private Long interviewCompletedDate;

    @Column(name = "result_received_date")
    private Long resultReceivedDate;

    @Column(name = "offer_accepted")
    private Boolean offerAccepted;

    @Column(name = "notes",columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Long createdAt;

    @Column(name = "updated_at",nullable = false)
    private Long updatedAt;


    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
        statusUpdatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = System.currentTimeMillis();
    }



}
