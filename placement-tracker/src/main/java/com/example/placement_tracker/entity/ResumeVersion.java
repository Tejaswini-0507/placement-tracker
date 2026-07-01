package com.example.placement_tracker.entity;

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
@Table(name = "resume_versions",indexes = {
        @Index(name = "idx_resume_student_date",columnList = "student_id, created_at DESC"),
        @Index(name = "idx_resume_student_version",columnList = "student_id, version_number")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ResumeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id",nullable = false)
    private Student student;

    @Column(name = "version_number",nullable = false)
    private Integer versionNumber;

    @Column(name = "file_url",nullable = false)
    private String fileUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Long createdAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "used_for_companies",columnDefinition = "jsonb")
    private JsonNode usedForCompanies;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
    }
}
