package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.PostItemDto;
import com.example.noticebespring.dto.main.EventMapDto;
import com.example.noticebespring.dto.main.TopViewDto;
import com.example.noticebespring.service.auth.UserService;
import com.example.noticebespring.service.mainpage.EventMapService;
import com.example.noticebespring.service.mainpage.RecentNoticeService;
import com.example.noticebespring.service.mainpage.TopViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
@Tag(name = "메인 API", description = "메인 페이지 API (월간 인기 공지, 모든 공지)")
public class MainPageController {
    private final RecentNoticeService recentNoticeService;
    private final TopViewService topViewService;
    private final EventMapService eventMapService;
    private final UserService userService;

    @Operation(
            summary = "월간 인기 공지 조회 ",
            description = "최근 30일 동안 '통합 공지'에서의 조회수 상위 7개 게시물 조회 ",
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
                                                            "createdAt": "2025-03-01T12:00:00",
                                                        },
                                                        {
                                                            "postId": 2,
                                                            "title": "공지 2",
                                                            "views": 450,
                                                            "createdAt": "2025-03-02T12:00:00",
                                                        },
                                                        {
                                                            "postId": 3,
                                                            "title": "공지 3",
                                                            "views": 420,
                                                            "createdAt": "2025-03-03T12:00:00",
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
                                                            "createdAt": "2025-03-06T12:00:00",
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
                                                        "errorCode": "40402",
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
                                                        "errorCode": "50000",
                                                        "message": "서버 내부 오류입니다."
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
    @GetMapping("/top")
    public CommonResponse<List<TopViewDto>> getTop7PostsByBoardName() {
        try {
            List<TopViewDto> topViewDtoList = topViewService.getTop7PostsByBoardName("통합공지");
            return CommonResponse.ok(topViewDtoList);
        } catch (EntityNotFoundException e){
            return CommonResponse.fail(ErrorCode.NOT_FOUND_POST);
        } catch (Exception e) {
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "모든 공지 조회 ",
            description = "메인 페이지에서 모든 공지의 최근 7개 게시물 조회 ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                                {
                                                     "success": true,
                                                     "data": [
                                                         {
                                                             "id": 19,
                                                             "category": "통합공지",
                                                             "title": "[한미교육위원단] 2026-2027년도 풀브라이트 미국 대학원 유학 및 포스트닥 장학 프로그램 안내",
                                                             "viewCount": 1939,
                                                             "hasReference": true,
                                                             "postedDate": "2025-03-18",
                                                             "isBookmarked": false,
                                                             "isPostedToday": true
                                                         },
                                                         {
                                                             "id": 20,
                                                             "category": "통합공지",
                                                             "title": "[중랑구] 패션봉제실무 및 창업·마케팅 교육 수강생 모집 안내",
                                                             "viewCount": 1398,
                                                             "hasReference": true,
                                                             "postedDate": "2025-03-16",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 21,
                                                             "category": "통합공지",
                                                             "title": "[학생복지팀] 25 상반기 재단법인 춘천시민장학재단 봄내장학생 선발 안내",
                                                             "viewCount": 2003,
                                                             "hasReference": true,
                                                             "postedDate": "2025-03-14",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 22,
                                                             "category": "통합공지",
                                                             "title": "2025년 상반기 민주화운동기념사업회 청년인턴 채용 공고 홍보 안내",
                                                             "viewCount": 1662,
                                                             "hasReference": true,
                                                             "postedDate": "2025-03-12",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 23,
                                                             "category": "통합공지",
                                                             "title": "[경기콘텐츠진흥원] 2025년 경기게임아카데미 창업과정 14기 모집 안내",
                                                             "viewCount": 1431,
                                                             "hasReference": true,
                                                             "postedDate": "2025-03-10",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 24,
                                                             "category": "통합공지",
                                                             "title": "[세종특별자치시] 2025년 세종특별자치시 공공데이터 활용 창업경진대회",
                                                             "viewCount": 1531,
                                                             "hasReference": true,
                                                             "postedDate": "2025-03-08",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 25,
                                                             "category": "통합공지",
                                                             "title": "[자유전공학부대학] 2025학년도 자유전공학부대학 선후배이어주기 재학생 멘토모집",
                                                             "viewCount": 2844,
                                                             "hasReference": true,
                                                             "postedDate": "2025-03-05",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         }
                                                     ],
                                                     "error": null
                                                 }
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
                                                        "errorCode": "40402",
                                                        "message": "게시물을 찾을 수 없습니다."
                                                    }
                                                }
                                            """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "모든 공지 조회에 대한 내부 오류", content = {
                            @Content(
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
                    })
            },
            parameters = {
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true)
            }
    )
    @GetMapping("/recent")
    public CommonResponse<List<PostItemDto>> getRecent7Posts() {
        try {
            List<PostItemDto> postItemDtoList = recentNoticeService.getRecentPosts();
            return CommonResponse.ok(postItemDtoList);
        } catch (EntityNotFoundException e) {
            return CommonResponse.fail(ErrorCode.NOT_FOUND_POST);
        } catch (Exception e) {
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "지도 이벤트 조회 ",
            description = "오늘 날짜 기준으로 종료되지 않은(진행 예정이거나 현재 진행 중인) 이벤트 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                                {
                                                     "success": true,
                                                     "data": [
                                                         {
                                                             "id": 19,
                                                             "category": "통합공지",
                                                             "title": "[한미교육위원단] 2026-2027년도 풀브라이트 미국 대학원 유학 및 포스트닥 장학 프로그램 안내",
                                                             "viewCount": 1939,
                                                             "hasReference": true,
                                                             "startDate": "2025-03-18",
                                                             "endDate": "2025-03-21",
                                                             "isBookmarked": false,
                                                             "isPostedToday": true
                                                         },
                                                         {
                                                             "id": 20,
                                                             "category": "통합공지",
                                                             "title": "[중랑구] 패션봉제실무 및 창업·마케팅 교육 수강생 모집 안내",
                                                             "viewCount": 1398,
                                                             "hasReference": true,
                                                             "startDate": "2025-03-19",
                                                             "endDate": "2025-03-24",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 21,
                                                             "category": "통합공지",
                                                             "title": "[학생복지팀] 25 상반기 재단법인 춘천시민장학재단 봄내장학생 선발 안내",
                                                             "viewCount": 2003,
                                                             "hasReference": true,
                                                             "startDate": "2025-03-25",
                                                             "endDate": "2025-03-28",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 22,
                                                             "category": "통합공지",
                                                             "title": "2025년 상반기 민주화운동기념사업회 청년인턴 채용 공고 홍보 안내",
                                                             "viewCount": 1662,
                                                             "hasReference": true,
                                                             "startDate": "2025-04-10",
                                                             "endDate": "2025-04-12",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 23,
                                                             "category": "통합공지",
                                                             "title": "[경기콘텐츠진흥원] 2025년 경기게임아카데미 창업과정 14기 모집 안내",
                                                             "viewCount": 1431,
                                                             "hasReference": true,
                                                             "startDate": "2025-04-18",
                                                             "endDate": "2025-04-20",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 24,
                                                             "category": "통합공지",
                                                             "title": "[세종특별자치시] 2025년 세종특별자치시 공공데이터 활용 창업경진대회",
                                                             "viewCount": 1531,
                                                             "hasReference": true,
                                                             "startDate": "2025-04-19",
                                                             "endDate": "2025-04-22",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         },
                                                         {
                                                             "id": 25,
                                                             "category": "통합공지",
                                                             "title": "[자유전공학부대학] 2025학년도 자유전공학부대학 선후배이어주기 재학생 멘토모집",
                                                             "viewCount": 2844,
                                                             "hasReference": true,
                                                             "startDate": "2025-04-25",
                                                             "endDate": "2025-04-30",
                                                             "isBookmarked": false,
                                                             "isPostedToday": false
                                                         }
                                                     ],
                                                     "error": null
                                                 }
                                                }
                                            """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "활성화된 이벤트 조회에 대한 내부 오류", content = {
                            @Content(
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
                    })
            },
            parameters = {
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true)
            }
    )
    @GetMapping("/event")
    public CommonResponse<List<EventMapDto>> getMapEvents(){
        Integer userId = userService.getAuthenticatedUser().getId();
        try{
            List<EventMapDto> events = eventMapService.getEventsVisibleOnMap(userId);
            if (events.isEmpty()){
                log.info("활성화된 이벤트가 존재하지 않음 - {}개", events.size());
            }
            return CommonResponse.ok(events);
        } catch (Exception e){
            log.error("서버 내부 오류 발생 - user: {}, error: {}", userId, e.getMessage());
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

