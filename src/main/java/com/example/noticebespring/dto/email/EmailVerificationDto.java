package com.example.noticebespring.dto.email;

import jakarta.validation.constraints.NotEmpty;

public record EmailVerificationDto(
        @NotEmpty(message = "Email cannot be empty")
        String email,
        @NotEmpty(message = "Verification Code cannot be empty")
        String verificationCode
) {
}