package com.example.noticebespring.common.filter;

import com.example.noticebespring.service.auth.jwt.JwtService;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JWT로 API 요청시 검증하는 필터

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().startsWith("/api/auth/login/")) {
            log.info("Bypassing filter for /api/auth/login/");
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            log.error("Authorization header is missing or does not start with 'Bearer '");
            // 예외로 던져서 EntryPoint로 넘김
            AuthenticationException authEx = new AuthenticationServiceException("Missing or invalid Authorization header");
            jwtAuthenticationEntryPoint.commence(request, response, authEx);
            return;
        }

        String token = header.substring(7);

        try {
            if (jwtService.isTokenValid(token)) {
                Integer userId = jwtService.extractUserId(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("User authenticated successfully. User ID: {}", userId);
            } else {
                log.warn("유효하지 않은 JWT 토큰 - URI: {}", request.getRequestURI());
                throw new BadCredentialsException("유효하지 않은 JWT 토큰입니다. ");
            }
        } catch (Exception e) {
            log.error("JWT 예외 발생 - URI: {}", request.getRequestURI(), e);
            AuthenticationException authEx = new AuthenticationServiceException("JWT 인증 실패", e);
            jwtAuthenticationEntryPoint.commence(request, response, authEx);
            return;
        }

        //다음 필터로 요청 전달
        filterChain.doFilter(request, response);

    }

    // 메인, 로그인 화면, 인증 URL은 필터링에서 제외함
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean shouldNotFilter = request.getMethod().equalsIgnoreCase("OPTIONS") ||
                request.getRequestURI().startsWith("/api/auth/login/") ||
                request.getRequestURI().startsWith("/swagger-ui") ||
                request.getRequestURI().startsWith("/v3/api-docs") ||
                request.getRequestURI().startsWith("/api-docs") ||
                request.getRequestURI().startsWith("/api/test001") ||
                request.getRequestURI().equals("/") ||
                request.getRequestURI().equals("/login") ||
                request.getRequestURI().startsWith("/api/auth/sneaky/register") ||
                request.getRequestURI().startsWith("/api/auth/sneaky/login");
        log.info("shouldNotFilter for URI {}: {}", request.getRequestURI(), shouldNotFilter);
        return shouldNotFilter;
    }
}
