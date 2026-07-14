package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.DiscussionMessage;
import com.example.placement_tracker.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, UUID> {

   //Find by Company
    List<DiscussionThread> findByCompany_Id(UUID companyId);
    List<DiscussionThread> findByCompany_idOrderByLastActivityDesc(UUID companyId);

    //Find By company nad interview round
    List<DiscussionThread> findByCompany_IdAndInterviewRound(UUID companyId, String interviewRound);

    //Find pinned threads
    List<DiscussionThread> findByCompany_IdAndPinned(UUID companyId, Boolean pinned);

    //Find By creator
    List<DiscussionThread> findByCreatedByStudent_Id(UUID studentId);

    //Find by topic
    List<DiscussionThread> findByCompany_idAndTopic(UUID companyId, String topic);

    //Search threads
    List<DiscussionThread> findByCompany_IdAndTitleContainingIgnoreCase(UUID companyId, String title);

    // Count
    long countByCompany_Id(UUID companyId);

}
