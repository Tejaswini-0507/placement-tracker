package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.Position;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.entity.StudentApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentApplicationRepository extends JpaRepository<StudentApplication , UUID> {

    @EntityGraph(attributePaths = {"student","company"})
    List<StudentApplication> findByStudent_Id(UUID studentId);

    @EntityGraph(attributePaths = {"company"})
    List<StudentApplication> findByCompany_Id(UUID companyId);

    List<StudentApplication> findByPosition_Id(UUID positionId);

    Optional<StudentApplication> findByStudent_IdAndCompany_IdAndPosition_Id(UUID studentId,UUID companyId, UUID positionId);
    boolean existsByStudentAndPosition(Student student, Position position);

    List<StudentApplication> findByPosition_Title(String positionTitle);
}
