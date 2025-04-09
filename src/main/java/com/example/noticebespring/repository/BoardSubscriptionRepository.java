package com.example.noticebespring.repository;

import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoDto;
import com.example.noticebespring.entity.BoardSubscription;
import com.example.noticebespring.entity.BoardSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface BoardSubscriptionRepository extends JpaRepository<BoardSubscription, BoardSubscriptionId> {

    /**
     * 특정 사용자 ID에 해당하는 모든 게시판 구독 정보를 조회한다.
     *
     * @param userId 사용자 ID
     * @return       해당 사용자의 구독 정보 리스트
     */
    @Query("SELECT b FROM BoardSubscription b WHERE b.user.id = :userId")
    List<BoardSubscription> findByUserId(Integer userId);



    /**
     * 주어진 boardId와 postType 목록에 해당하는 구독 정보를 가진 유저들을 조회한다.
     * 결과는 유저 ID, 이메일, 게시판 ID, 게시물 타입으로 구성된 DTO로 반환된다.
     *
     * @param boardId    게시판 ID
     * @param postTypes  게시물 타입 리스트 (예: 공지, 질문 등)
     * @return           구독 정보 DTO 리스트
     */
    @Query("""
    SELECT new com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoDto(u.id, u.email, bs.id.boardId, bs.id.postType)
    FROM BoardSubscription bs
    JOIN bs.user u
    WHERE bs.id.boardId = :boardId
      AND bs.id.postType IN :postTypes
    ORDER BY u.id
""")
    List<UserSubscriptionInfoDto> findUserSubscriptionInfoByBoardIdAndPostTypes(
            @Param("boardId") Integer boardId,
            @Param("postTypes") List<String> postTypes
    );
}
