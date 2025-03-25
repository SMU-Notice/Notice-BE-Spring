package com.example.noticebespring.repository;

import com.example.noticebespring.entity.BoardSubscription;
import com.example.noticebespring.entity.BoardSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardSubscriptionRepository extends JpaRepository<BoardSubscription, BoardSubscriptionId> {

    List<BoardSubscription> findByUserId(Integer userId);

    void deleteByUserIdAndBoardId(Integer userId, Integer boardId);

    List<BoardSubscription> findByUserIdAndBoardId(Integer userId, Integer boardId);

    // 1개 이상의 originalPostId로 검색
    List<BoardSubscription> findByOriginalPostIdIn(List<Integer> originalPostIds);
}
