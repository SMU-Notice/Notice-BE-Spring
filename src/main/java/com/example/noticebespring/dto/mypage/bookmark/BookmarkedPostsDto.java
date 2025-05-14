package com.example.noticebespring.dto.mypage.bookmark;

import java.util.List;

// 폴더 내의 북마크된 게시물 목록
public record BookmarkedPostsDto(
        String name,
        List<BookmarkedPostItemDto> posts
) {
}
