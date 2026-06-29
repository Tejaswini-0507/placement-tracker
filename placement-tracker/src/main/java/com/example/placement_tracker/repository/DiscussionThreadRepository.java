package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, String> {
    List<DiscussionThread> findByCompanyId(String companyId);
    List<DiscussionThread> findByStudentId(String studentId);
    List<DiscussionThread> findBYCompanyIdAndInterviewRound(String comapnyId, String interviewRound);
}
