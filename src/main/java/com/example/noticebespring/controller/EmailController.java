package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.ApiResponse;
import com.example.noticebespring.dto.email.EmailDto;
import com.example.noticebespring.dto.email.EmailVerificationDto;
import com.example.noticebespring.service.email.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @PostMapping("/verification/send")
    @Operation(
            summary = "인증 코드 발송 (로그인 필요)",
            description = "사용자의 이메일로 인증 코드를 발송합니다."
           )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = ""),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<String> mailSend(@RequestBody EmailDto emailDto) throws MessagingException {
        log.info("EmailController.mailSend()");
        emailService.sendEmail(emailDto);
        String message = "인증 코드가 발송되었습니다.";
        return ApiResponse.ok(message);
    }

    @PostMapping("/verification/verify")
    @Operation(
            summary = "인증 코드 검증 (로그인 필요)",
            description = "입력한 인증 코드가 맞는지 확인합니다."
            )
            @ApiResponses({
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "인증 코드 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"data\": null,\n  \"error\": {\n    \"code\": 40002,\n    \"message\": \"인증 코드 오류\"\n  }\n}")
                            )),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<String> verify(@RequestBody EmailVerificationDto emailVerificationDtoDto) {
        log.info("EmailController.verify()");
        emailService.verifyEmailCode(emailVerificationDtoDto);
        return ApiResponse.ok("인증이 완료되었습니다.");
    }

}

