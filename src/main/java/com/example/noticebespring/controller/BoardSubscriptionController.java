package com.example.noticebespring.controller;


import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostNotificationRequestDto;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionRequestDto;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionResponseDto;
import com.example.noticebespring.service.boardSubscription.BoardSubscriptionNotificationService;
import com.example.noticebespring.service.boardSubscription.BoardSubscriptionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/board-subscription")
@RestController
@Tag(name = "게시판 구독 API", description = "게시판 구독 관련 API")
public class BoardSubscriptionController {

    private final BoardSubscriptionManagementService boardSubscriptionManagementService;
    private final BoardSubscriptionNotificationService boardSubscriptionNotificationService;

    @Operation(
            summary = "구독 목록 조회",
            description = "사용자가 구독한 게시판 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionResponseDto.class)
                    )
            )
    })
    @GetMapping
    public CommonResponse<SubscriptionResponseDto> getSubscriptions() {
        SubscriptionResponseDto response = boardSubscriptionManagementService.getSubscriptions();
        return CommonResponse.ok(response);
    }


    @Operation(
            summary = "구독 관리 (구독 추가 및 수정)",
            description = "사용자가 구독할 게시판과 유형을 추가하거나 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 관리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "비어있는 구독 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = "{ \"success\": false, \"data\": null, \"error\": { \"code\": 40003, \"message\": \"구독 정보가 없습니다.\" } }")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 이메일을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = "{ \"success\": false, \"data\": null, \"error\": { \"code\": 40403, \"message\": \"이메일이 존재하지 않습니다.\" } }")
                    )
            )
    })
    @PostMapping
    public CommonResponse<SubscriptionResponseDto> manageSubscriptions(@RequestBody SubscriptionRequestDto subscriptionRequestDto) {
        SubscriptionResponseDto response = boardSubscriptionManagementService.manageSubscriptions(subscriptionRequestDto);
        return CommonResponse.ok(response);
    }


    @Operation(
            summary = "구독 전체 취소",
            description = "사용자가 구독한 모든 게시판의 구독을 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = "{ \"success\": true, \"data\": \"구독이 모두 취소되었습니다.\", \"error\": null }")
                    )
            ),
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "기존에 구독 내역이 없음",
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = CommonResponse.class),
//                            examples = @ExampleObject(value = "{ \"success\": true, \"data\": \"구독 정보가 없습니다.\", \"error\": null }")
//                    )
//            )
    })
    @DeleteMapping
    public CommonResponse<String> cancelSubscription() {
        String message = boardSubscriptionManagementService.cancelAllSubscription();
        return CommonResponse.ok(message);
    }

    @Operation(
            summary = "새 게시물 등록 알림 발송",
            description = "새로 등록된 게시물에 대해 구독자들에게 이메일 알림을 발송합니다. " +
                    "게시물 타입별로 구독한 사용자들에게만 해당 게시물 정보가 포함된 이메일이 전송됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이메일 알림 발송 요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": true,
                                  "data": "이메일 전송 요청을 받았습니다.",
                                  "error": null
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청한 게시물 중 일부가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": null,
                                  "error": {
                                    "code": 400010,
                                    "message": "요청한 게시물 중 일부가 존재하지 않습니다."
                                  }
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시판을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": null,
                                  "error": {
                                    "code": 40409,
                                    "message": "게시판을 찾을 수 없습니다."
                                  }
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Redis 캐시 처리 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": null,
                                  "error": {
                                    "code": 50005,
                                    "message": "Redis 캐시 처리에 실패했습니다."
                                  }
                                }
                                """
                            )
                    )
            )
    })
    @PostMapping("/new-posts")
    public CommonResponse<String> notifyNewPosts(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "새 게시물 알림 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostNotificationRequestDto.class),
                            examples = @ExampleObject(
                                    name = "새 게시물 알림 요청 예시",
                                    value = """
                                {
                                  "boardId": 1,
                                  "postTypes": {
                                    "학사": [1, 2, 3],
                                    "일반": [4, 5],
                                    "공지사항": [6]
                                  }
                                }
                                """
                            )
                    )
            )
            PostNotificationRequestDto requestDto) {
        boardSubscriptionNotificationService.sendNewPostNotification(requestDto);
        return CommonResponse.ok("이메일 전송 요청을 받았습니다.");
    }
}
