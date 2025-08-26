package com.example.noticebespring.dto.mypage.bookmark;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "폴더 내의 북마크된 게시물 목록")
public record BookmarkedPostsDto(

        @Schema(description = "폴더 이름")
        String name,

        @Schema(description = "북마크된 게시물 목록")
        List<BookmarkedPostItemDto> posts
) {
}
