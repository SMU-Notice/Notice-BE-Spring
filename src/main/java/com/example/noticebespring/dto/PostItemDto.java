package com.example.noticebespring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

//단일 게시물 항목에 대한 DTO
//boardName : 게시판 이름을 게시물 좌측의 아이콘으로 보여주기 위함
// ex) 통합, 학과, SW, ...
//북마크된 게시물에서는 (category = null)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostItemDto(
        Integer id,
        String boardName,
        String postType,
        String title,
        Integer viewCount,
        Boolean hasReference,
        LocalDate postedDate,
        Boolean isBookmarked,
        Boolean isPostedToday
) {
}
