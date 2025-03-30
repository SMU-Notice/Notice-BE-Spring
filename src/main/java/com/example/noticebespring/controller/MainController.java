package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.TopViewDto;
import com.example.noticebespring.service.MainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "메인 API", description = "메인 페이지 API (현재는 월간 인기 공지만 존재함)")
public class MainController {
    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @Operation(
            summary = "월간 인기 공지 조회",
            description = "최근 30일 동안 '통합 공지'에서의 조회수 상위 7개 게시물 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": true,
                                            "data": [
                                                {
                                                    "postId": 1,
                                                    "title": "공지 1",
                                                    "views": 500,
                                                    "createdAt": "2025-03-01T12:00:00"
                                                },
                                                {
                                                    "postId": 2,
                                                    "title": "공지 2",
                                                    "views": 450,
                                                    "createdAt": "2025-03-02T12:00:00"
                                                },
                                                {
                                                    "postId": 3,
                                                    "title": "공지 3",
                                                    "views": 420,
                                                    "createdAt": "2025-03-03T12:00:00"
                                                },
                                                {
                                                    "postId": 4,
                                                    "title": "공지 4",
                                                    "views": 400,
                                                    "createdAt": "2025-03-04T12:00:00"
                                                },
                                                {
                                                    "postId": 5,
                                                    "title": "공지 5",
                                                    "views": 390,
                                                    "createdAt": "2025-03-05T12:00:00"
                                                },
                                                {
                                                    "postId": 6,
                                                    "title": "공지 6",
                                                    "views": 370,
                                                    "createdAt": "2025-03-06T12:00:00"
                                                },
                                                {
                                                    "postId": 7,
                                                    "title": "공지 7",
                                                    "views": 350,
                                                    "createdAt": "2025-03-07T12:00:00"
                                                }
                                            ],
                                            "error": null
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "NOT_FOUND_POST",
                                                "message": "게시물을 찾을 수 없습니다."
                                            }
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "월간 인기 공지 조회에 대한 내부 오류", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "INTERNAL_SERVER_ERROR",
                                                "message": "서버 내부 오류"
                                            }
                                        }
                                    """)
                            )
                    })
            },
            parameters = {
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true)
            }
    )
    @GetMapping("/main")
    public CommonResponse<List<TopViewDto>> getTop7PostsByBoardName(){
        try {
            List<TopViewDto> topViewDtoList = mainService.getTop7PostsByBoardName("통합공지");
            return CommonResponse.ok(topViewDtoList);
        } catch (EntityNotFoundException e) {
            return CommonResponse.fail(ErrorCode.NOT_FOUND_POST);
        } catch (Exception e) {
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

