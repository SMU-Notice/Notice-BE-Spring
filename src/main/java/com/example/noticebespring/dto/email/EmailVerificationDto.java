package com.example.noticebespring.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record EmailVerificationDto(
        @NotBlank(message = "Email cannot be empty")
        @Schema(description = "사용자의 이메일 주소", example = "user@example.com")
        String email,
  
        @NotBlank(message = "Verification Code cannot be empty")
        @Schema(description = "이메일 인증 코드", example = "123456")
        String verificationCode
) {
}