package com.example.noticebespring.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Test Error
    TEST_ERROR(10000, HttpStatus.BAD_REQUEST, "테스트 에러입니다."),
    //Invalid Provider
    INVALID_PROVIDER(40000, HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 제공자입니다."),

    //401 Unauthorized
    UNAUTHORIZED(40100,HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다. 재로그인 하세요"),
    //401 Unauthorized(JWT Token Error)
    JWT_TOKEN_ERROR(40100, HttpStatus.UNAUTHORIZED, "JWT 토큰이 유효하지 않습니다"),
    // 404 Not Found
    NOT_FOUND_END_POINT(40400, HttpStatus.NOT_FOUND, "존재하지 않는 API입니다."),
    // 404 Not Found User
    NOT_FOUND_USER(40400, HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다"),
    // 500 JWT GENERATION FAILED
    JWT_GENERATION_FAILED(50000, HttpStatus.INTERNAL_SERVER_ERROR, "JWT 발급에 실패했습니다."),
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
