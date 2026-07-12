package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {
    //Get all resume version of a student
    List<ResumeVersion> findByStudent_Id(UUID studentId);

    //Get the latest uploaded resume
    Optional<ResumeVersion> findByStudent_IdOrderByCreatedAtDesc(UUID studentId);

    //Get all resume versions ordered by version number
    List<ResumeVersion> findByStudent_IdOrderByVersionNumberAsc(UUID studentId);

    //Get a specific version of a student's resume
    Optional<ResumeVersion> findByStudent_IdAndVersionNumber(UUID studentId, Integer versionNumber);
}
