package com.example.noticebespring.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record EmailDto(
        @NotEmpty(message = "Email cannot be empty")
        @Schema(description = "사용자의 이메일 주소", example = "user@example.com", required = true)
        String email
) {
}
