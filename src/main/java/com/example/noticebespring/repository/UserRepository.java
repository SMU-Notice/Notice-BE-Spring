package com.example.noticebespring.repository;

import com.example.noticebespring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(Integer userid);

    Optional<User> findByEmail(String email);
}

