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
    INVALID_VERIFY_CODE(40001, HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    NOT_FOUND_USER(40002, HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다."),

    // 404 Not Found
    NOT_FOUND_END_POINT(40400, HttpStatus.NOT_FOUND, "존재하지 않는 API입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
