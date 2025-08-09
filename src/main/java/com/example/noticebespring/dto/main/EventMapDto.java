package com.example.noticebespring.dto.main;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record EventMapDto(
        @Schema(description = "게시물 id")
        Integer id,

        @Schema(description = "이벤트 장소 이름", example = "미래백년관 5층 학생식당")
        String location,

        @Schema(description = "이벤트가 포함된 게시물 제목", example = "[학생복지팀] 2024학년도 2학기 기말고사 응원행사 천원의 아침밥 학생처가 쏜다 시행 안내")
        String title,

        @Schema(description = "원문 url")
        String url,

        @Schema(description = "게시 날짜")
        LocalDate postedDate,

        @Schema(description = "게시물 북마크 여부")
        Boolean isBookmarked
){
}
