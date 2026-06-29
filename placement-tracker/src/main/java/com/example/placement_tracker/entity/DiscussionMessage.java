package com.example.placement_tracker.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "discussion_messages",indexes = {
        @Index(name = "idx_msg_student",columnList = "student_id"),
        @Index(name = "idx_msg_thread",columnList = "thread_id, created_at")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class DiscussionMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private DiscussionThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id",nullable = false)
    private Student student;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String message;

    @Column(name = "edited_at")
    private Long editedAt;

    @Column(name = "likes")
    private Integer likes;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Long createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
        if(likes == null) likes = 0;
    }
}
