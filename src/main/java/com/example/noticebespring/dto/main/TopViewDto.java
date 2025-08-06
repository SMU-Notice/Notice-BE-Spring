package com.example.noticebespring.dto.main;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "이달의 인기글 DTO")
public record TopViewDto (

        @Schema(description = "게시물 id")
        Integer postId,

        @Schema(description = "게시물 제목")
        String title,

        @Schema(description = "원문 게시물 게시 날짜")
        LocalDate postedDate,

        @Schema(description = "원문 조회수")
        Integer viewCount
){}
