package com.example.noticebespring.dto.protestevent;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 단일 시위 정보(한 건의 정보만 담고 있음), 응답에는 장소, 일자, 시작시간, 종료시간 담고있음
 */
@Schema(description = "단일 시위 정보")

public record ProtestEventItemDto(
		@Schema(description = "장소(시위 장소)", example = "고궁박물관 - 창성동 별관")
        String location,
        
        @Schema(description = "시위 날짜", example = "2025-07-30")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate protestDate,
        
        @Schema(description = "시작 시각", example = "09:00")
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        
        @Schema(description = "종료 시각", example = "10:00")
        @JsonFormat(pattern = "HH:mm") LocalTime endTime
) {}
