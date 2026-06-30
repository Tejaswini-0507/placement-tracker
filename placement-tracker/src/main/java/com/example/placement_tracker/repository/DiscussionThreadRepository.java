package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, String> {
    List<DiscussionThread> findByCompany_Id(String companyId);
    List<DiscussionThread> findByCreatedByStudent_Id(String studentId);
    List<DiscussionThread> findByCompany_IdAndInterviewRound(String companyId, String interviewRound);
}
