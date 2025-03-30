package com.example.noticebespring.dto;

import java.time.LocalDate;

//게시물 목록에서의 단일 게시물 항목
public record PostItemDto(
        Integer id,
        String title,
        Integer viewCount,
        String url,
        Boolean hasReference,
        LocalDate postedDate,
        boolean isBookmarked
) {
}
