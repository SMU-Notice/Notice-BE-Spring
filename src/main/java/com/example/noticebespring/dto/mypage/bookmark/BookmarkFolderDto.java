package com.example.noticebespring.dto.mypage.bookmark;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "북마크 폴더 DTO")
public record BookmarkFolderDto(

        @Schema(description = "폴더 id")
        Integer id,

        @Schema(description = "폴더 이름")
        String name,

        @Schema(description = "폴더 생성 시각")
        LocalDateTime createdAt
) {}
