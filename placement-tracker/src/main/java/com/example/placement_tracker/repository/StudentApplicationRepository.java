package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.StudentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface StudentApplicationRepository extends JpaRepository<StudentApplication , String> {

    List<StudentApplication> findByStudent_Id(String studentId);
    List<StudentApplication> findByCompany_Id(String companyId);
    Optional<StudentApplication> findByStudent_IdAndCompany_Id(String studentId, String companyId);

}
