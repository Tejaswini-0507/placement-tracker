package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.StudentPositionApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentPositionApplicationRepository extends JpaRepository<StudentPositionApplication, UUID > {

    List<StudentPositionApplication> findByStudent_Id(UUID studentId);
    List<StudentPositionApplication> findByPosition_Id(UUID positionId);
    Optional<StudentPositionApplication> findByStudent_IdAndPosition_Id(UUID studentId, UUID positionId);
}
