package com.example.placement_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "discussion_threads" , indexes = {
        @Index(name = "idx_thread_company_round", columnList = "company_id , interview_round"),
        @Index(name = "idx_thread_creator",columnList = "created_by_student_id"),
        @Index(name = "idx_thread_pinned_activity",columnList = "pinned,last_activity DESC")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DiscussionThread {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id",nullable = false)
    private Company company;

    @Column(name = "interview_round")
    private String interviewRound;

    @Column(name = "topic")
    private String topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_student_id" ,nullable = false)
    private Student createdByStudent;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Boolean pinned;

    @Column(name = "message_count")
    private Integer messageCount;

    @Column(name = "last_activity")
    private Long lastActivity;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @OneToMany(mappedBy = "thread",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DiscussionMessage> messages = new ArrayList<>();

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
        lastActivity = System.currentTimeMillis();
        pinned = false;
        messageCount = 0;
    }

}
