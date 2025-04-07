package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.service.auth.UserService;
import com.example.noticebespring.service.mypage.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

//모든 공지, 상세 게시물 페이지에서의 게시물 북마크 추가 및 제거 컨트롤러
@Slf4j
@RestController
@RequestMapping("/api/bookmark")
@Tag(name = "게시물 북마크 추가 제거 API", description = "게시물별 북마크 추가 및 제거 기능 (모든 공지, 상세 게시물 페이지에 적용)")
@RequiredArgsConstructor
public class PostBookmarkController {
    private final BookmarkService bookmarkService;
    private final UserService userService;

    @Operation(
            summary = "게시물 북마크 추가",
            description = "모든 공지 및 상세 게시물에서 단일 게시물에 북마크 추가",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공 (성공 시 북마크 id 반환)", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": true,
                                                "data": 1,
                                                "error": null
                                            }
                                        """)
                            )
                    }),
                    @ApiResponse(responseCode = "409", description = "이미 북마크된 게시물", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                                {
                                                    "success": false,
                                                    "data": null,
                                                    "error": {
                                                        "errorCode": "40902",
                                                        "message": "이미 북마크된 게시물입니다."
                                                    }
                                                }
                                            """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "게시물 북마크 추가 실패", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                                {
                                                    "success": false,
                                                    "data": null,
                                                    "error": {
                                                        "errorCode": "50004",
                                                        "message": "게시물 북마크 추가에 실패했습니다."
                                                    }
                                                }
                                            """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "북마크 추가에 대한 내부 오류", content = {
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
                    @Parameter(name = "folderId", description = "북마크 대상 게시물을 저장할 폴더의 id", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "postId", description = "북마크 대상 게시물의 id", in = ParameterIn.PATH, required = true)
            }
    )
    @PostMapping("/add/{folderId}/{postId}")
    public CommonResponse<Integer> addBookmark(@PathVariable("folderId") Integer folderId, @PathVariable("postId") Integer postId){
        log.info("Received request: folderId={}, postId={}", folderId, postId);
        Integer userId = userService.getAuthenticatedUser().getId();

        try {
            Integer bookmarkId = bookmarkService.addBookmark(userId, folderId, postId);
            if (bookmarkId == null) {
                return CommonResponse.fail(ErrorCode.ADD_BOOKMARK_ERROR);
            }
            return CommonResponse.created(bookmarkId);
        } catch (CustomException e){
            return CommonResponse.fail(ErrorCode.EXISTS_ALREADY_BOOKMARK);
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "게시물 북마크 제거",
            description = "모든 공지 및 상세 게시물에서 단일 게시물에 북마크 제거",
            responses = {
                    @ApiResponse(responseCode = "200", description = "제거 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": true,
                                                "data": null,
                                                "error": null
                                            }
                                        """)
                            )
                    }),
                    @ApiResponse(responseCode = "404", description = "게시물에서 북마크를 찾을 수 없음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                                {
                                                    "success": false,
                                                    "data": null,
                                                    "error": {
                                                        "errorCode": "40406",
                                                        "message": "북마크를 찾을 수 없습니다."
                                                    }
                                                }
                                            """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "북마크 제거에 대한 내부 오류", content = {
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
                    @Parameter(name = "folderId", description = "북마크된 게시물을 제거할 폴더의 id", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "postId", description = "북마크를 제거할 게시물의 id", in = ParameterIn.PATH, required = true)
            }
    )
    @DeleteMapping("/remove/{folderId}/{postId}")
    public CommonResponse<Void> removeBookmark(@PathVariable("folderId") Integer folderId, @PathVariable("postId") Integer postId){
        Integer userId = userService.getAuthenticatedUser().getId();
        
        try {
            bookmarkService.removeBookmark(userId, folderId, postId);
            return CommonResponse.ok(null);
        } catch (CustomException e){
            return CommonResponse.fail(ErrorCode.NOT_FOUND_BOOKMARK);
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

