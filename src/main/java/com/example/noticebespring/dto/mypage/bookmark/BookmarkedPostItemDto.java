package com.example.noticebespring.dto.mypage.bookmark;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "북마크된 단일 게시물에 대한 전용 DTO")
public record BookmarkedPostItemDto(
        Integer id,
        String title,
        Integer viewCount,
        Boolean hasReference,
        LocalDate postedDate,
        Boolean isBookmarked
) {}
