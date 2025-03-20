package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.ApiResponse;
import com.example.noticebespring.dto.NewPostDto;
import com.example.noticebespring.dto.email.EmailDto;
import com.example.noticebespring.dto.email.EmailVerificationDto;
import com.example.noticebespring.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
@RestController
public class EmailController {
    private final EmailService emailService;

    /**
     * 인증코드가 담긴 메일 송신 API
     * @param emailDto
     * @return
     * @throws MessagingException
     */
    @PostMapping("/send")
    public ApiResponse<String> mailSend(@RequestBody EmailDto emailDto) throws MessagingException {
        log.info("EmailController.mailSend()");
        emailService.sendEmail(emailDto);
        String message = "인증코드가 발송되었습니다.";
        return ApiResponse.ok(message);
    }


    /**
     * 인증 코드 일치 여부 검증 API
     * @param emailDto
     * @return
     */
    @PostMapping("/verify")
    public ApiResponse<String> verify(@RequestBody EmailVerificationDto emailDto) {
        log.info("EmailController.verify()");
        emailService.verifyEmailCode(emailDto);
        return ApiResponse.ok("인증이 완료되었습니다.");
    }

    @PostMapping("/notify/posts")
    public ApiResponse<String> notifyNewPosts(@RequestBody NewPostDto newPostDto) {
        log.info("EmailController.verify()");
        return ApiResponse.ok("메일 전송이 완료되었습니다.");
    }



}

