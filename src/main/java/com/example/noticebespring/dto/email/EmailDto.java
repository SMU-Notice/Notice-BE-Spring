package com.example.noticebespring.dto.email;

import jakarta.validation.constraints.NotEmpty;

public record EmailDto(
        @NotEmpty(message = "Email cannot be empty")
        String email
) {
}
