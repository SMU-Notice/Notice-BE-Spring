package com.example.noticebespring.dto.sneaky;

import jakarta.validation.constraints.NotBlank;

public record UserRegisterDto(
        @NotBlank(message = "Enter an email")
        String email,

        @NotBlank(message = "Enter a password")
        String password
) {
}