package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.StudentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Struct;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentApplicationRepository extends JpaRepository<StudentApplication , String> {

    List<StudentApplication> findByStudentId(String studentId);
    List<StudentApplication> findByCompanyId(String companyId);
    Optional<StudentApplication> findByStudentIdAndCompanyId(String studentId, String companyId);

}
