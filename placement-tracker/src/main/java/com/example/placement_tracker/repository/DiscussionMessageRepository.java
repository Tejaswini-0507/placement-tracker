package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.DiscussionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, String> {
    List<DiscussionMessage> findByThreadIdOrderByCreatedAtAsc(String threadId);


}
