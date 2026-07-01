package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.StudentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentApplicationRepository extends JpaRepository<StudentApplication , UUID> {

    List<StudentApplication> findByStudent_Id(UUID studentId);
    List<StudentApplication> findByCompany_Id(UUID companyId);
    Optional<StudentApplication> findByStudent_IdAndCompany_Id(UUID studentId, UUID companyId);

}
