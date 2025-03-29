package com.example.noticebespring.repository;

import com.example.noticebespring.entity.BoardSubscription;
import com.example.noticebespring.entity.BoardSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardSubscriptionRepository extends JpaRepository<BoardSubscription, BoardSubscriptionId> {

    @Query("SELECT b FROM BoardSubscription b WHERE b.user.id = :userId")
    List<BoardSubscription> findByUserId(Integer userId);



}
