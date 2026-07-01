package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.DiscussionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, UUID> {
    List<DiscussionMessage> findByThread_IdOrderByCreatedAtAsc(UUID threadId);

    List<DiscussionMessage> findByStudent_IdOrderByCreatedAtAsc(UUID studentId);

}
