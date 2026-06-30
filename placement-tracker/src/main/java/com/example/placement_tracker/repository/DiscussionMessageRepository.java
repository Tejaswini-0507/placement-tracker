package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.DiscussionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, String> {
    List<DiscussionMessage> findByThread_IdOrderByCreatedAtAsc(String threadId);

    List<DiscussionMessage> findByStudent_IdOrderByCreatedAtAsc(String studentId);

}
