package com.example.noticebespring.common.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public record ApiResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,

        @Schema(description = "API 요청 성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 데이터", nullable = true)
        @Nullable T data,

        @Schema(description = "에러 정보 (정상 응답 시 null)", nullable = true)
        @Nullable ExceptionDto error
) {

    public static <T> ApiResponse<T> ok(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> ApiResponse<T> created(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.CREATED, true, data, null);
    }

    public static <T> ApiResponse<T> fail(final ErrorCode c) {
        return new ApiResponse<>(c.getHttpStatus(), false, null, ExceptionDto.of(c));
    }
}