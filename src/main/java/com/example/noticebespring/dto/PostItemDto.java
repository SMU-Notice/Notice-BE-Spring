package com.example.noticebespring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

//
//boardName : 게시판 이름을 게시물 좌측의 아이콘으로 보여주기 위함
// ex) 통합, 학과, SW, ...
//북마크된 게시물에서는 (category = null)
@Schema(description = "단일 게시물 항목에 대한 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostItemDto(

        @Schema(description = "게시물 ID")
        Integer id,

        @Schema(description = "게시판 이름", example = "통합공지")
        String boardName,

        @Schema(description = "게시물 종류", example = "학사")
        String postType,

        @Schema(description = "게시물 제목")
        String title,

        @Schema(description = "원문 조회수")
        Integer viewCount,

        @Schema(description = "첨부파일 여부")
        Boolean hasReference,

        @Schema(description = "원문 게시물 게시 날짜")
        LocalDate postedDate,

        @Schema(description = "게시물 북마크 여부")
        Boolean isBookmarked,

        @Schema(description = "오늘 올라온 게시물 여부")
        Boolean isPostedToday
) {
}
