package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, UUID> {
    List<DiscussionThread> findByCompany_Id(UUID companyId);
    List<DiscussionThread> findByCreatedByStudent_Id(UUID studentId);
    List<DiscussionThread> findByCompany_IdAndInterviewRound(UUID companyId, String interviewRound);
}
