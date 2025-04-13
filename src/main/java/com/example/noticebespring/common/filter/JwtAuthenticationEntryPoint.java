package com.example.noticebespring.common.filter;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 인증되지 않은 사용자의 요청에 대해 401 Unauthorized 응답을 반환
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("Unauthorized access attempt to {}", request.getRequestURI());

        ErrorCode errorCode = getErrorCode(authException);

        CommonResponse<?> commonResponse = CommonResponse.fail(errorCode);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), commonResponse);
    }

    // 예외 종류에 따라 적절한 ErrorCode를 반환
    private ErrorCode getErrorCode(AuthenticationException authException) {
        Throwable cause = authException.getCause();

        if (cause == null) {
            log.error("Authentication Failed - cause not found", authException);
            return ErrorCode.UNAUTHORIZED;
        }
        else if (cause instanceof ExpiredJwtException) {
            log.error("JWT token expired", cause);
            return ErrorCode.JWT_TOKEN_EXPIRED;
        } else if (cause instanceof SignatureException) {
            log.error("JWT signature invalid", cause);
            return ErrorCode.JWT_SIGNATURE_INVALID;
        } else if (cause instanceof MalformedJwtException) {
            log.error("JWT malformed", cause);
            return ErrorCode.JWT_TOKEN_ERROR;
        } else {
            log.error("JWT authentication failed", cause);
            return ErrorCode.UNAUTHORIZED;
        }
    }
}

