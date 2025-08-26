package com.example.noticebespring.dto.protestevent;

import io.swagger.v3.oas.annotations.media.Schema;

/** 기준일(today)과 다음날(tomorrow) 두 날짜의 시위 정보 */
@Schema(description = "기준일과 다음날(오늘/내일) 두 날짜 시위 정보")
public record ProtestEventAllResponseDto(
        @Schema(description = "오늘의 시위 정보")
        ProtestEventDailyDto today,

        @Schema(description = "내일의 시위 정보")
        ProtestEventDailyDto tomorrow
) {}