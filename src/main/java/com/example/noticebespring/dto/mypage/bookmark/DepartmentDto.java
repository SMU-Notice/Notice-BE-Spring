package com.example.noticebespring.dto.mypage.bookmark;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepartmentDto(
        String collegeName,
        String departmentName
) {
}
