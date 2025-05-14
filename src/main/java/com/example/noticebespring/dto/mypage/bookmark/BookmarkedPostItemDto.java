package com.example.noticebespring.dto.mypage.bookmark;

import java.time.LocalDate;

// 북마크된 게시물 한 개에 대한 전용 DTO
public record BookmarkedPostItemDto(
        Integer id,
        String title,
        Integer viewCount,
        Boolean hasReference,
        LocalDate postedDate,
        Boolean isBookmarked
) {}
