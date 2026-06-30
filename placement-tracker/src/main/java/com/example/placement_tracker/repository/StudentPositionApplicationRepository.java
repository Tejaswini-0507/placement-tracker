package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.StudentPositionApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentPositionApplicationRepository extends JpaRepository<StudentPositionApplication, String > {

    List<StudentPositionApplication> findByStudent_Id(String studentId);
    List<StudentPositionApplication> findByPosition_Id(String positionId);
    Optional<StudentPositionApplication> findByStudent_IdAndPositionId(String studentId, String positionId);
}
