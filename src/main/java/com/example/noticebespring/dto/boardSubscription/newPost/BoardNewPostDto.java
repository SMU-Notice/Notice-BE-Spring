package com.example.noticebespring.dto.boardSubscription.newPost;

import java.time.LocalDateTime;
import java.util.List;

public record BoardNewPostDto(
        String siteName,           // 게시판 사이트 이름
        String boardName,          // 게시판 이름
        List<Post> posts           // 해당 게시판의 게시물 목록
) {
    public record Post(
            String postName,         // 게시물 이름
            String postType,         // 게시물 종류 (예: 공지, 질문 등)
            String postSummary,      // 게시물 본문 요약
            Boolean hasReference,    // 게시물 참고 자료 여부
            LocalDateTime postDate   // 게시물 게시 날짜
    ) {}
}
