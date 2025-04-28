package com.example.noticebespring.repository;

import com.example.noticebespring.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Integer> {
    Optional<College> findByName(String name);
}
