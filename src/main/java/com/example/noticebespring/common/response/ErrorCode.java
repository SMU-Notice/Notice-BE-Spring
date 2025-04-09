package com.example.noticebespring.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Test Error
    TEST_ERROR(10000, HttpStatus.BAD_REQUEST, "테스트 에러입니다."),

    // 400 Bad Request
    INVALID_PROVIDER(40001, HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 제공자입니다."),
    INVALID_VERIFY_CODE(40002, HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    INVALID_SUBSCRIPTION_REQUEST(40003, HttpStatus.BAD_REQUEST, "구독 정보가 없습니다."),
    INVALID_VERIFY_FOLDER_NAME(40004, HttpStatus.BAD_REQUEST, "폴더의 이름이 유효하지 않습니다."),
    INVALID_POST_ID_REQUEST(40005, HttpStatus.BAD_REQUEST, "요청한 게시물 중 일부가 존재하지 않습니다."),

    //401 Unauthorized
    UNAUTHORIZED(40101,HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다. 재로그인 하세요."),
    JWT_TOKEN_ERROR(40102, HttpStatus.UNAUTHORIZED, "JWT 토큰이 유효하지 않습니다."),
    JWT_TOKEN_EXPIRED(40103, HttpStatus.UNAUTHORIZED, "JWT 토큰이 만료되었습니다."),
    JWT_SIGNATURE_INVALID(40104, HttpStatus.UNAUTHORIZED, "JWT 토큰의 서명이 올바르지 않습니다."),

    //403 Forbiddem
    FORBIDDEN(40300, HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 404 Not Found
    NOT_FOUND_END_POINT(40400, HttpStatus.NOT_FOUND, "존재하지 않는 API입니다."),
    NOT_FOUND_USER(40401, HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다"),
    NOT_FOUND_POST(40402, HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다."),
    EMAIL_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "이메일이 존재하지 않습니다."),
    NOT_FOUND_SUBSCRIPTION(40404, HttpStatus.NOT_FOUND, "구독 내역이 존재하지 않습니다."),
    NOT_FOUND_FOLDER(40405, HttpStatus.NOT_FOUND, "폴더를 찾을 수 없습니다."),
    NOT_FOUND_BOARD(40405, HttpStatus.NOT_FOUND, "게시판을 찾을 수 없습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    JWT_GENERATION_FAILED(50001, HttpStatus.INTERNAL_SERVER_ERROR, "JWT 발급에 실패했습니다.");


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}