package com.example.noticebespring.controller;

import com.example.noticebespring.dto.PostResponseDto;
import com.example.noticebespring.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.noticebespring.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping("/api/main/posts")
@RequiredArgsConstructor
public class PostController {
	
    private final PostService postService;

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID에 따라 상세 게시물을 조회합니다.",
            responses = {
                @ApiResponse(
                    responseCode = "200",
                    description = "게시글 상세 조회 성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                            {
                              "success": true,
                              "data": {
                                "postId": 82,
                                "title": "[수정공지] HUSS 인문사회융합인재양성사업단 2025학년도 1학기 수강신청 및 학사일정 안내",
                                "contentSummary": "게시글의 전체 내용 ...",
                                "url": "https://www.smu.ac.kr/kor/life/notice.do?mode=view&articleNo=752895...",
                                "hasReference": true,
                                "isBookmarked": false,
                                "pictureSummary": "사진에 대한 요약",
                                "viewCount": 0,
                                "postedDate": "2025-03-29"
                                "previousPost":{
                                "postId":81,
                                "title":"이전 게시글 제목"
                              },
                              "nextPost":{
                              "postId":83,
                              "title":"다음 게시글 제목"
                              }
                             },
                              "error": null
                            }
                            """)
                    )
                ),
                @ApiResponse(
                    responseCode = "404",
                    description = "게시글을 찾을 수 없음",
                    content = @Content(
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
                ),
                @ApiResponse(
                    responseCode = "500",
                    description = "내부 서버 오류",
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
            },
            parameters = {
                @Parameter(
                    name = "postId",
                    description = "게시글 ID",
                    in = ParameterIn.PATH
                )
            }
        )
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostResponseDto>> getPostResponse(
    		@PathVariable("postId") Integer postId) {
        PostResponseDto dto = postService.getPostResponse(postId);
        return ResponseEntity.ok(CommonResponse.ok(dto));
    }

}

