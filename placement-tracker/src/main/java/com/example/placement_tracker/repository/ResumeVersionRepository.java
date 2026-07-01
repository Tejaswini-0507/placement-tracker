package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {
    List<ResumeVersion> findByStudent_IdOrderByVersionNumberDesc(UUID studentId);
}
