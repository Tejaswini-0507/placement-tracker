package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.DiscussionMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, UUID> {

    //Find by thread
    @EntityGraph(attributePaths = {"thread","student"})
    List<DiscussionMessage> findByThread_IdOrderByCreatedAtAsc(UUID threadId);

    List<DiscussionMessage> findByStudent_IdOrderByCreatedAtAsc(UUID studentId);


    List<DiscussionMessage> findByThread_Id(UUID threadId);

    // Find by student
    List<DiscussionMessage> findByStudent_Id(UUID studentId);

    // Count
    long countByThread_Id(UUID threadId);

    // Find messages after specific timestamp (for polling)
    @EntityGraph(attributePaths = {"thread","student"})
    List<DiscussionMessage> findByThread_IdAndCreatedAtGreaterThanOrderByCreatedAtAsc(UUID threadId, Long createdAt);

}
