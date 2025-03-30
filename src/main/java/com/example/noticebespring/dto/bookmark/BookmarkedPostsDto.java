package com.example.noticebespring.dto.bookmark;

import com.example.noticebespring.dto.PostItemDto;
import java.util.List;

// 폴더 내의 북마크된 게시물 목록
public record BookmarkedPostsDto(
        String name,
        List<PostItemDto> posts
) {
}
