package com.example.noticebespring.controller;


import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionRequestDto;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionResponseDto;
import com.example.noticebespring.service.BoardSubscriptionService;
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

    private final BoardSubscriptionService boardSubscriptionService;

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
        SubscriptionResponseDto response = boardSubscriptionService.getSubscriptions();
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
        SubscriptionResponseDto response = boardSubscriptionService.manageSubscriptions(subscriptionRequestDto);
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
        String message = boardSubscriptionService.cancelAllSubscription();
        return CommonResponse.ok(message);
    }

//    @PostMapping("/send-new-posts")
//    public void sendNewPostsEmail(@RequestBody SubscriptionEmailRequestDto subscriptionEmailRequestDto) {
//        log.info("New posts received for subscription: {}", subscriptionEmailRequestDto);
//
//        // 이메일 전송을 위한 처리
//        emailService.sendNewPostNotification(subscriptionEmailRequestDto.newPosts());
//
//        log.info("New posts subscription email sent successfully.");
//    }


}
