package com.example.noticebespring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.noticebespring.entity.Board;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
}
