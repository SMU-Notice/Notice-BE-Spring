package com.example.noticebespring.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "모든 공지 페이지 응답 DTO")
public record AllNoticeResponseDto(
        @Schema(description = "전체 페이지 수")
        int totalPages,
        @Schema(description = "조회된 게시물 목록")
        List<PostItemDto> posts
) {
}
