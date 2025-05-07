package com.example.noticebespring.dto.mypage;

import io.swagger.v3.oas.annotations.media.Schema;

public record DepartmentDto(

        @Schema(description = "단과대학 이름", example = "융합공과대학")
        String collegeName,

        @Schema(description = "학과 이름", example = "컴퓨터과학전공")
        String departmentName
) {
}
