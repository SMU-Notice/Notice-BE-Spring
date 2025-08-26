package com.example.noticebespring.dto.protestevent;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ProtestEventDailyDto(
        @Schema(description = "해당 날짜", example = "2025-08-24")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @Schema(description = "해당 날짜의 등록된 시위 개수", example = "2")
        int count,

        @Schema(description = "시위 일정 목록")
        List<ProtestEventItemDto> events,

        @Schema(description = "시위 정보가 없을 시 안내문", example = "아직 시위정보가 없습니다. (19:00 이후 업데이트)", nullable = true)
        String message
) {}