package com.example.noticebespring.repository;

import com.example.noticebespring.entity.College;
import com.example.noticebespring.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findByNameAndCollege(String Name, College college);
}
