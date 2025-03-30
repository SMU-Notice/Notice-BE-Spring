package com.example.noticebespring.common.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public record CommonResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,

        @Schema(description = "API 요청 성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 데이터", nullable = true)
        @Nullable T data,

        @Schema(description = "에러 정보 (정상 응답 시 null)", nullable = true)
        @Nullable ExceptionDto error
) {

    public static <T> CommonResponse<T> ok(@Nullable final T data) {
        return new CommonResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> CommonResponse<T> created(@Nullable final T data) {
        return new CommonResponse<>(HttpStatus.CREATED, true, data, null);
    }

    public static <T> CommonResponse<T> fail(final ErrorCode c) {
        return new CommonResponse<>(c.getHttpStatus(), false, null, ExceptionDto.of(c));
    }
}