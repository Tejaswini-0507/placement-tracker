package com.example.placement_tracker.entity;


import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "interview_experiences", indexes = {
        @Index(name = "idx_exp_company_round" , columnList = "company_id, interview_round_config_id"),
        @Index(name = "idx_exp_difficulty" , columnList = "difficulty_rating"),
        @Index(name = "idx_exp_student_company",columnList = "student_id, company_id"),
        @Index(name = "idx_exp_upvotes",columnList = "upvotes DESC")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterviewExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id" , nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id" , nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_application_id")
    private StudentApplication studentApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_round_config_id")
    private InterviewRoundConfig interviewRoundConfig;

    @Column(name = "round_number")
    private Integer roundNumber;

    @Column(name = "date_experienced",nullable = false)
    private Long dateExperienced;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_rating")
    private DifficultyLevel difficultyRating;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name ="total_problems_asked")
    private Integer totalProblemsAsked;

    @Column(name = "questions_asked",columnDefinition = "TEXT")
    private String questionsAsked;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions_json",columnDefinition = "jsonb")
    private JsonNode questionsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode topics;

    @Column(name = "experience_summary", columnDefinition = "TEXT")
    private String experienceSummary;

    @Column(name = "helpful_resources", columnDefinition = "TEXT")
    private String helpfulResources;

    @Column(name = "interviewer_feedback", columnDefinition = "TEXT")
    private String interviewerFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private InterviewResult result;

    @Column(name = "result_received_date")
    private Long resultReceivedDate;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "upvotes")
    private Integer upvotes;

    @Column(name = "downvotes")
    private Integer downvotes;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Long createdAt;

    @Column(name = "updated_at",nullable = false)
    private Long updatedAt;

    @Column(name = "published_at")
    private Long publishedAt;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
        if(upvotes == null) upvotes = 0;
        if(downvotes == null) downvotes = 0;
        if(isPublic == null) isPublic = true;
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = System.currentTimeMillis();
    }

}
