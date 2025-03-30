package com.example.noticebespring.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record EmailVerificationDto(
        @Schema(description = "사용자의 이메일 주소", example = "user@example.com", required = true)
        @NotEmpty(message = "Email cannot be empty")
        String email,

        @Schema(description = "이메일 인증 코드", example = "123456")
        @NotEmpty(message = "Verification Code cannot be empty")
        String verificationCode
) {
}