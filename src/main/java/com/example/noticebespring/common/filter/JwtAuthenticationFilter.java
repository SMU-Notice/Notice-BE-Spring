package com.example.noticebespring.common.filter;

import com.example.noticebespring.service.auth.jwt.JwtService;
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
import java.util.Collections;

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

//        log.info("==== [요청 확인] ====");
//        log.info("requestURL     = {}", request.getRequestURL());
//        log.info("requestURI     = {}", request.getRequestURI());
//        log.info("servletPath    = {}", request.getServletPath());
//        log.info("queryString    = {}", request.getQueryString());
//        log.info("method         = {}", request.getMethod());
//        log.info("User-Agent     = {}", request.getHeader("User-Agent"));
//        log.info("Referer        = {}", request.getHeader("Referer"));

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
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("SecurityContext에 Authentication 설정 완료. principal={}, authorities={}",
                        authentication.getPrincipal(),
                        authentication.getAuthorities());
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
        log.info("==== [요청 확인] ====");
        log.info("requestURL     = {}", request.getRequestURL());
        log.info("requestURI     = {}", request.getRequestURI());
        log.info("servletPath    = {}", request.getServletPath());
        log.info("queryString    = {}", request.getQueryString());
        log.info("method         = {}", request.getMethod());
        log.info("User-Agent     = {}", request.getHeader("User-Agent"));
        log.info("Referer        = {}", request.getHeader("Referer"));
        String path = request.getServletPath();
        boolean shouldNotFilter =
                request.getMethod().equalsIgnoreCase("OPTIONS") ||
                        path.equals("/") ||
                        path.equals("/login") ||
                        path.equals("/favicon.ico") ||
                        path.startsWith("/auth") ||
                        path.startsWith("/swagger-ui") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/api-docs") ||
                        path.startsWith("/api/auth/login/") ||
                        path.startsWith("/api/test001") ||
                        path.equals("/api/auth/sneaky/login") ||
                        path.equals("/api/auth/sneaky/register") ||
                        // 외부 봇 탐지용 요청 무시
                        path.endsWith(".aspx") ||
                        path.startsWith("/Core/Skin/") ;
                ;
        log.info("shouldNotFilter for servletPath {}: {} ", path, shouldNotFilter);
        return shouldNotFilter;
    }
}
