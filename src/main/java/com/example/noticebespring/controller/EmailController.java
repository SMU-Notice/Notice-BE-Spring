package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.dto.email.EmailDto;
import com.example.noticebespring.dto.email.EmailVerificationDto;
import com.example.noticebespring.service.email.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/email")
@RestController
@Tag(name = "이메일 API", description = "이메일 관련 API")
public class EmailController {
    private final EmailService emailService;

    @Operation(
            summary = "인증 코드 발송 (로그인 필요)",
            description = "사용자의 이메일로 인증 코드를 발송합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 코드 발송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = "{ \"success\": true, \"data\": \"인증 코드가 발송되었습니다.\", \"error\": null }")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "이메일 전송 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = "{ \"success\": false, \"data\": null, \"error\": { \"code\": 50000, \"message\": \"서버 내부 오류입니다.\" } }")
                    )
            )
    })

  
    @PostMapping("/verification/send")
    public CommonResponse<String> mailSend(@RequestBody EmailDto emailDto) throws MessagingException {
        log.info("EmailController.mailSend()");
        emailService.sendVerificationEmail(emailDto);
        String message = "인증 코드가 발송되었습니다.";
        return CommonResponse.ok(message);
    }


    @Operation(
            summary = "인증 코드 검증 (로그인 필요)",
            description = "입력한 인증 코드가 맞는지 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 코드 검증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = "{ \"success\": true, \"data\": \"인증이 완료되었습니다.\", \"error\": null }")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "인증 코드 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = "{ \"success\": false, \"data\": null, \"error\": { \"code\": 40002, \"message\": \"유효하지 않은 인증 코드입니다.\" } }")
                    )
            )
    })
    @PostMapping("/verification/verify")
    public CommonResponse<String> verify(@RequestBody EmailVerificationDto emailVerificationDto) {
        log.info("EmailController.verify()");
        emailService.verifyEmailCode(emailVerificationDto);
        return CommonResponse.ok("인증이 완료되었습니다.");
    }

}

