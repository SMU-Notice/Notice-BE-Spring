package com.example.noticebespring.controller;
import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.AllNoticeResponseDto;
import com.example.noticebespring.dto.PostItemDto;
import com.example.noticebespring.service.auth.UserService;
import com.example.noticebespring.service.notice.AllNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/main")
@Tag(name = "모든 공지 API", description = "모든 공지 페이지에 대한 API")
@RequiredArgsConstructor
public class AllNoticeController {
    private final UserService userService;
    private final AllNoticeService allNoticeService;

    @Operation(
            summary = "모든 공지 페이지 조회",
            description = "필터링 조건이 제공되면 조건에 맞는 게시물 반환, 조건이 없으면 전체 게시물 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시물 조회 성공. 필터링 조건 유무에 따라 결과가 달라짐",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AllNoticeResponseDto.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "FilteredPostsWithTotalPages",
                                                    summary = "전체 게시물 목록 예시 (페이지 수 포함)",
                                                    value = """
                                                        {
                                                          "success": true,
                                                          "data": {
                                                            "totalPages": 42,
                                                            "posts": [
                                                              {
                                                                "id": 27,
                                                                "boardName": "통합공지",
                                                                "title": "[학생복지팀] 서울시 병원 안심동행 서비스 안내",
                                                                "viewCount": 3093,
                                                                "hasReference": false,
                                                                "postedDate": "2025-03-13",
                                                                "isBookmarked": false,
                                                                "isPostedToday": false
                                                              },
                                                              {
                                                                "id": 30,
                                                                "boardName": "통합공지",
                                                                "title": "[한미교육위원단] 2025년 글로벌 인턴십 프로그램 모집 안내",
                                                                "viewCount": 1243,
                                                                "hasReference": true,
                                                                "postedDate": "2025-03-12",
                                                                "isBookmarked": false,
                                                                "isPostedToday": false
                                                              },
                                                              {
                                                                "id": 28,
                                                                "boardName": "통합공지",
                                                                "title": "[학생복지팀] 2025년 상반기 학자금 대출 이자 지원 안내",
                                                                "viewCount": 1210,
                                                                "hasReference": true,
                                                                "postedDate": "2025-03-12",
                                                                "isBookmarked": false,
                                                                "isPostedToday": false
                                                              },
                                                              {
                                                                "id": 31,
                                                                "boardName": "통합공지",
                                                                "title": "[학생복지팀] 2025년 하반기 대학생 학자금 대출 이자 지원 안내",
                                                                "viewCount": 2021,
                                                                "hasReference": true,
                                                                "postedDate": "2025-03-12",
                                                                "isBookmarked": false,
                                                                "isPostedToday": false
                                                              },
                                                              {
                                                                "id": 29,
                                                                "boardName": "통합공지",
                                                                "title": "[한국산업인력공단] 2025년 하반기 직무능력개발 훈련 과정 안내",
                                                                "viewCount": 890,
                                                                "hasReference": true,
                                                                "postedDate": "2025-03-12",
                                                                "isBookmarked": false,
                                                                "isPostedToday": false
                                                              },
                                                              {
                                                                "id": 35,
                                                                "boardName": "통합공지",
                                                                "title": "[자유전공학부대학] 2025년 자유전공학부대학 모집 안내",
                                                                "viewCount": 1573,
                                                                "hasReference": true,
                                                                "postedDate": "2025-03-11",
                                                                "isBookmarked": false,
                                                                "isPostedToday": false
                                                              },
                                                              {
                                                                "id": 33,
                                                                "boardName": "통합공지",
                                                                "title": "[서울시청] 2025년 서울시 청년 인턴 프로그램 안내",
                                                                "viewCount": 1504,
                                                                "hasReference": true,
                                                                "postedDate": "2025-03-11",
                                                                "isBookmarked": false,
                                                                "isPostedToday": false
                                                              },
                                                              {
                                                                "id": 34,
                                                                "boardName": "통합공지",
                                                                "title": "[학생복지팀] 2025년 서울시 대학생 학자금 이자 지원 안내",
                                                                "viewCount": 1802,
                                                                "hasReference": false,
                                                                "postedDate": "2025-03-11",
                                                                "isBookmarked": false,
                                                                "isPostedToday": false
                                                              }
                                                            ]
                                                          },
                                                          "error": null
                                                        }
                                                        """
                                            )
                                    })}),
                    @ApiResponse(responseCode = "400", description = "페이지 번호가 유효하지 않음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                                {
                                                    "success": false,
                                                    "data": null,
                                                    "error": {
                                                        "errorCode": "40405",
                                                        "message": "페이지 번호값이 유효하지 않습니다."
                                                    }
                                                }
                                            """)
                            )
                    }),
                    @ApiResponse(responseCode = "400", description = "페이지 크기가 유효하지 않음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                                {
                                                    "success": false,
                                                    "data": null,
                                                    "error": {
                                                        "errorCode": "40406",
                                                        "message": "페이지 크기값이 유효하지 않습니다."
                                                    }
                                                }
                                            """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "게시물 조회 실패", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                                {
                                                    "success": false,
                                                    "data": null,
                                                    "error": {
                                                        "errorCode": "50002",
                                                        "message": "게시물 조회에 실패했습니다."
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
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true),
                    @Parameter(name = "page", description = "조회할 페이지 번호", in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "size", description = "조회할 페이지 크기", in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "boardName", description = "게시판 이름 (ex. 통합공지, 학과, ...)", in = ParameterIn.QUERY, required = false),
                    @Parameter(name = "postType", description = "게시물 종류 (ex. 장학금, 학사, ...)", in = ParameterIn.QUERY, required = false),
                    @Parameter(name = "searchTerm", description = "검색어 (게시물 제목에 대한 검색어 필터)", in = ParameterIn.QUERY, required = false),
                    @Parameter(name = "startDate", description = "게시 날짜 필터 (시작일) - 종료일도 같이 보내야 함 ", in = ParameterIn.QUERY, required = false),
                    @Parameter(name = "endDate", description = "게시 날짜 필터 (종료일) - 시작일도 같이 보내야 함", in = ParameterIn.QUERY, required = false)
            }
    )
    //전체 게시물 및 필터링된 게시물 조회
    @GetMapping("/board")
    public CommonResponse<AllNoticeResponseDto> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String boardName,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate

    ){
        if(page < 0){
            log.warn("유효하지 않은 페이지 번호 - pageNumber: {}", page);
            return CommonResponse.fail(ErrorCode.INVALID_PAGE_NUMBER);
        }
        if(size <= 0){
            log.warn("유효하지 않은 페이지 크기 - pageSize: {}", size);
            return CommonResponse.fail(ErrorCode.INVALID_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(page, size);
        Integer userId = userService.getAuthenticatedUser().getId();

        boolean hasFilters = boardName != null || postType != null || searchTerm != null || startDate != null || endDate != null;

        try{
            List<PostItemDto> posts = allNoticeService.getAllFilteredPosts(userId, pageable, boardName, postType, searchTerm, startDate, endDate);

            Integer totalCount = posts.size(); // 전체 게시물 수
            Integer totalPages = (int) Math.ceil((double) totalCount / size); // size는 pageSize

            if(posts.isEmpty()){
                if(hasFilters) {
                    log.info("조건에 맞는 게시물 존재하지 않음 - user: {}, boardName: {}, postType: {}, searchTerm: {}, startDate: {}, endDate: {}",
                            userId, boardName, postType, searchTerm, startDate, endDate);
                }else{
                    log.info("게시물이 존재하지 않음 - user: {}", userId);
                }
                totalPages = 1;
            }
            if(hasFilters) {
                log.info("조건에 맞는 게시물 조회 성공 - user: {}, postCount: {}, boardName: {}, postType: {}, searchTerm: {}, startDate: {}, endDate: {}",
                        userId, posts.size(), boardName, postType, searchTerm, startDate, endDate);
            }else{
                log.info("전체 게시물 조회 성공 - user: {}, postCount: {}", userId, posts.size());
            }

            //전체 페이지 수 + 조회된 게시물 리스트
            AllNoticeResponseDto response = new AllNoticeResponseDto(totalPages, posts);

            return CommonResponse.ok(response);

        }catch (CustomException e){
            if (hasFilters) {
                log.error("필터링된 게시물 조회 실패 - user: {}, error: {}", userId, e.getMessage());
            }else{
                log.error("전체 게시물 조회 실패 - user: {}, error: {}", userId, e.getMessage());
            }
            return CommonResponse.fail(ErrorCode.POST_RETRIEVAL_ERROR);
        }catch (Exception e){
            log.error("서버 내부 오류 발생 - user: {}, error: {}", userId, e.getMessage());
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
