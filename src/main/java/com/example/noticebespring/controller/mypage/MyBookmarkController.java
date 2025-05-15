package com.example.noticebespring.controller.mypage;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.mypage.bookmark.BookmarkFolderDto;
import com.example.noticebespring.dto.mypage.bookmark.BookmarkedPostsDto;
import com.example.noticebespring.service.auth.UserService;
import com.example.noticebespring.service.mypage.BookmarkFolderService;
import com.example.noticebespring.service.mypage.BookmarkService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/mypage/bookmark")
@RequiredArgsConstructor
@Tag(name = "북마크 페이지 API", description = "사용자 별로 북마크 페이지 지원 (복마크 폴더 목록, 복마크된 게시물)")
public class MyBookmarkController {
    private final BookmarkFolderService folderService;
    private final BookmarkService bookmarkService;
    private final UserService userService;


    @Operation(
            summary = "북마크 폴더 조회",
            description = "사용자 만든 북마크 폴더 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                    {
                                        "success": true,
                                        "data": [
                                            {
                                                "id": 1,
                                                "name": "장학금",
                                                "createdAt": "2025-03-30T12:34:56"
                                            },
                                            {
                                                "id": 2,
                                                "name": "수강신청",
                                                "createdAt": "2025-03-30T12:35:00"
                                            }
                                        ],
                                        "error": null
                                    }
                                """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "북마크 폴더 목록 조회에 대한 내부 오류", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "50000",
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
    @GetMapping("")
    public CommonResponse<List<BookmarkFolderDto>> getBookmarkFolders() {
        //인증된 사용자의 id 가져오기
        Integer userId = userService.getAuthenticatedUser().getId();

        // 북마크 폴더 목록 조회
        try {
            List<BookmarkFolderDto> folders = folderService.getBookmarkFolders(userId);
            return CommonResponse.ok(folders);
        } catch (Exception e) {
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "새 폴더 생성",
            description = "새로운 북마크 폴더 생성 (폴더 이름은 '새 폴더', 폴더 이름 변경은 이름 변경 API에서 진행)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "변경 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": true,
                                            "data": {
                                                "id": 3,
                                                "name": "새 폴더",
                                                "createdAt": "2025-03-30T12:36:00"
                                            },
                                            "error": null
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "북마크 폴더 생성에 대한 내부 오류", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "50000",
                                                "message": "서버 내부 오류"
                                            }
                                        }
                                    """)
                            )
                    }
                    )
            },
            parameters = {
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true)
            }
    )
    @PostMapping("")
    public CommonResponse<BookmarkFolderDto> createNewBookmarkFolder(){
        Integer userId = userService.getAuthenticatedUser().getId();

        try {
            BookmarkFolderDto newfolder = folderService.createBookmarkFolder(userId);
            return CommonResponse.created(newfolder);
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "북마크 폴더 이름 변경",
            description = "사용자가 만든 북마크 폴더의 이름 변경",
            responses = {
                    @ApiResponse(responseCode = "200", description = "변경 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": true,
                                            "data": {
                                                "id": 1,
                                                "name": "새로운 폴더 이름",
                                                "createdAt": "2025-03-30T12:36:00"
                                            },
                                            "error": null
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "409", description = "폴더 이름이 중복됨", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "40901",
                                                "message": "동일한 이름의 폴더가 이미 존재합니다."
                                            }
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "403", description = "폴더 이름 변경에 대한 권한이 없음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "40300",
                                                "message": "권한이 없습니다."
                                            }
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "404", description = "folderId에 해당하는 폴더를 찾을 수 없음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "40405",
                                                "message": "폴더를 찾을 수 없습니다."
                                            }
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "북마크 폴더 이름 변경에 대한 내부 오류", content ={
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "50000",
                                                "message": "서버 내부 오류"
                                            }
                                        }
                                    """)
                            )
                    }

                    )
            },
            parameters = {
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true),
                    @Parameter(name = "folderId", description = "폴더의 id",in = ParameterIn.PATH, required = true),
                    @Parameter(name = "newName", description = "새 폴더 이름",in = ParameterIn.QUERY, required = true)
            }
    )
    @PatchMapping("/{folderId}")
    public CommonResponse<BookmarkFolderDto> updateBookmarkFolderName(@PathVariable("folderId") Integer folderId, @RequestParam String newName) {
        log.info("Received request: folderId={}, newName={}", folderId, newName);
        Integer userId = userService.getAuthenticatedUser().getId();
        try {
            BookmarkFolderDto updateFolder = folderService.updateBookmarkFolderName(userId, folderId, newName);
            return CommonResponse.ok(updateFolder);
        } catch (EntityNotFoundException e){
            return CommonResponse.fail(ErrorCode.NOT_FOUND_FOLDER);
        } catch (CustomException e){
            return CommonResponse.fail(ErrorCode.EXISTS_ALREADY_FOLDER_NAME);
        } catch (AccessDeniedException e){
            return CommonResponse.fail(ErrorCode.FORBIDDEN);
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "북마크 폴더 삭제",
            description = "사용자가 만든 북마크 폴더 삭제",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공", content = {
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
                    @ApiResponse(responseCode = "403", description = "폴더 삭제에 대한 권한이 없음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "40300",
                                                "message": "권한이 없습니다."
                                            }
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "404", description = "folderId에 해당하는 폴더를 찾을 수 없음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "40405",
                                                "message": "폴더를 찾을 수 없습니다."
                                            }
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "북마크 폴더 삭제에 대한 내부 오류", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "50000",
                                                "message": "서버 내부 오류"
                                            }
                                        }
                                    """)
                            )
                    })
            },
            parameters = {
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true),
                    @Parameter(name = "folderId", description = "폴더의 id",in = ParameterIn.PATH, required = true)
            }
    )
    @DeleteMapping("/{folderId}")
    public CommonResponse<Void> deleteBookmarkFolder(@PathVariable("folderId") Integer folderId){
        Integer userId = userService.getAuthenticatedUser().getId();

        try {
            folderService.deleteBookmarkFolder(userId, folderId);
            return CommonResponse.ok(null);
        } catch (EntityNotFoundException e){
            return CommonResponse.fail(ErrorCode.NOT_FOUND_FOLDER);
        } catch (AccessDeniedException e){
            return CommonResponse.fail(ErrorCode.FORBIDDEN);
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    @Operation(
            summary = "폴더 내 북마크된 게시물 조회",
            description = "단일 폴더의 북마크된 게시물 조회(북마크된 게시물이 없을 경우 빈 리스트 반환)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": true,
                                            "data": [
                                                {
                                                    "id": 1,
                                                    "title": "2차 수강신청",
                                                    "viewCount": "2910",
                                                    "hasReference": "true",
                                                    "postedDate": "2025-03-30T12:34:56"
                                                    "isBookmarked": "true"
                                                },
                                                {
                                                    "id": 2,
                                                    "title": "장학금 안내",
                                                    "viewCount": "4732",
                                                    "hasReference": "false",
                                                    "postedDate": "2025-03-30T12:35:00",
                                                    "isBookmarked": "true"
                                                }
                                            ],
                                            "error": null
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "404", description = "폴더가 존재하지 않음", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "40405",
                                                "message": "폴더를 찾을 수 없습니다."
                                            }
                                        }
                                    """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "폴더 내 북마크된 게시물 조회에 대한 내부 오류", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                            "success": false,
                                            "data": null,
                                            "error": {
                                                "errorCode": "50000",
                                                "message": "서버 내부 오류"
                                            }
                                        }
                                    """)
                            )
                    })
            },
            parameters = {
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true),
                    @Parameter(name = "folderId", description = "폴더의 id",in = ParameterIn.PATH, required = true)
            }
    )
    @GetMapping("/{folderId}/posts")
    public CommonResponse<BookmarkedPostsDto> getBookmarkedPosts(@PathVariable Integer folderId){
        Integer userId = userService.getAuthenticatedUser().getId();

        try {
            BookmarkedPostsDto posts = bookmarkService.getBookmarkedPosts(userId, folderId);
            return CommonResponse.ok(posts);
        } catch (CustomException e){
            return CommonResponse.fail(ErrorCode.NOT_FOUND_FOLDER);
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
