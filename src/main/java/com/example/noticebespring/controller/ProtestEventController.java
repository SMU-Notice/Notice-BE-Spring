package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.dto.protestevent.ProtestEventAllResponseDto;
import com.example.noticebespring.service.protestevent.ProtestEventService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/protest-events")

@Tag(name = "시위 정보 API", description = "시위 일정 조회 API")

public class ProtestEventController {

    private final ProtestEventService service;

    /**
     * GET /api/protest-events
     * 서울 시각 기준으로 오늘 및 내일의 시위 정보를 반환합니다.
     *  시위 정보가 하나도 없을 시엔 message에 "시위 일정이 존재하지 않습니다." 표시
     */
    @Operation(
            summary = "오늘/내일 시위 일정 조회",
            description = """
                서울 시각 기준 오늘 및 내일의 시위 정보를 반환합니다.
                - 현재 시각이 19시 이전이면 "아직 시위 일정이 없습니다. (19:00 이후 업데이트됩니다.)" 로 표시됩니다.
                - 시위 정보가 없어 비어 있는 경우에는 "시위 일정이 존재하지 않습니다." 로 표시됩니다.
                """,
            responses = {
                @ApiResponse(
                    responseCode = "200",
                    description = "조회에 성공한 경우",
                    content = @Content(
                        mediaType = "application/json",
                        examples = {
                            // 1) 오늘, 내일 모두 시위 정보가 있는 경우
                            @ExampleObject(
                                name = "오늘/내일 모두 시위정보가 존재하는 경우",
                                value = """
                                {
                                  "success": true,
                                  "data": {
                                    "today": {
                                      "date": "2025-08-24",
                                      "count": 2,
                                      "events": [
                                        { "location": "정부서울청사 - 세종대로사거리", "protestDate": "2025-08-24", "startTime": "14:00", "endTime": "16:00" },
                                        { "location": "광화문KT - 광화문R",         "protestDate": "2025-08-24", "startTime": "18:30", "endTime": "20:00" }
                                      ],
                                      "message": null
                                    },
                                    "tomorrow": {
                                      "date": "2025-08-25",
                                      "count": 1,
                                      "events": [
                                        { "location": "경복궁역 7번출구 앞", "protestDate": "2025-08-25", "startTime": "11:00", "endTime": "12:00" }
                                      ],
                                      "message": null
                                    }
                                  },
                                  "error": null
                                }
                                """
                            ),
                            // 2) 19시 이전 조회해 오늘의 시위 정보만 존재하는 경우
                            @ExampleObject(
                                name = "19시 이전에 조회한 경우",
                                value = """
                                {
                                  "success": true,
                                  "data": {
                                    "today": {
                                      "date": "2025-08-24",
                                      "count": 2,
                                      "events": [
                                        { "location": "정부서울청사 - 세종대로사거리", "protestDate": "2025-08-24", "startTime": "14:00", "endTime": "16:00" },
                                        { "location": "광화문KT - 광화문R",         "protestDate": "2025-08-24", "startTime": "18:30", "endTime": "20:00" }
                                      ],
                                      "message": null
                                    },
                                    "tomorrow": {
                                      "date": "2025-08-25",
                                      "count": 0,
                                      "events": [],
                                      "message": "아직 시위 일정이 없습니다. (19:00 이후 업데이트됩니다.)"
                                    }
                                  },
                                  "error": null
                                }
                                """
                            ),
                            // 3) 오늘 시위정보가 아예 없는 경우 (내일은 있는 경우)
                            @ExampleObject(
                                name = "오늘 시위정보가 없는 경우",
                                value = """
                                {
                                  "success": true,
                                  "data": {
                                    "today": {
                                      "date": "2025-08-24",
                                      "count": 0,
                                      "events": [],
                                      "message": "시위 일정이 존재하지 않습니다."
                                    },
                                    "tomorrow": {
                                      "date": "2025-08-25",
                                      "count": 1,
                                      "events": [
                                        { "location": "경복궁역 7번출구 앞", "protestDate": "2025-08-25", "startTime": "11:00", "endTime": "12:00" }
                                      ],
                                      "message": null
                                    }
                                  },
                                  "error": null
                                }
                                """
                            )
                        }
                    )
                ),
                @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                        {
                          "success": false,
                          "data": null,
                          "error": {
                            "errorCode": "50000",
                            "message": "서버 내부 오류입니다."
                          }
                        }
                        """)
                    )
                )
            }
        )
    @GetMapping
    public CommonResponse<ProtestEventAllResponseDto> getAllDays() {
        LocalDate base = service.todaySeoul(); // 서울 시각 기준
        return CommonResponse.ok(service.getAllDays(base));
    }
}