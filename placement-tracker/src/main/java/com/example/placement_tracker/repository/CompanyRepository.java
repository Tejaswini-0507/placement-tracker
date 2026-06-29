package com.example.placement_tracker.repository;

import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company , String> {

    Optional<Company> findByName(String name);
    List<Position> findPositions(String companyId);
}
