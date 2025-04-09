package com.example.noticebespring.repository;

import com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto;
import com.example.noticebespring.repository.Qrepository.PostRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.noticebespring.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>, PostRepositoryCustom {
    /**
     * 주어진 게시글 ID 목록에 해당하는 게시글 요약 정보를 조회한다.
     *
     * @param postIds 게시글 ID 리스트
     * @return        게시글 요약 정보 리스트
     */
    @Query("""
    SELECT new com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto(
        p.board.id, p.id,  p.type, p.title, p.contentSummary, p.postedDate
    )
    FROM Post p
    WHERE p.id IN :postIds
    ORDER BY p.postedDate DESC
""")
    List<PostSummaryDto> findPostSummariesByPostIds(@Param("postIds") List<Integer> postIds);
}
