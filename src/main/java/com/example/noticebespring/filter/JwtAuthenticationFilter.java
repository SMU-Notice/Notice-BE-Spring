package com.example.noticebespring.filter;

import com.example.noticebespring.service.auth.jwt.JwtService;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JWT로 API 요청시 검증하는 필터

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Autherization");
        if (header == null || !header.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // JWT 토큰 추출
        if (jwtService.isTokenValid(token)){
            Integer userId = jwtService.extractUserId(token);

            UsernamePasswordAuthenticationToken authentication
                    = new UsernamePasswordAuthenticationToken(userId,null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }else{
            // JWT 유효성 검증 실패

           throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

    }

    // 메인, 로그인 화면, 인증 URL은 필터링에서 제외함
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/auth/login")||
                request.getRequestURI().equals("/")||
                request.getRequestURI().equals("/login");
    }
}
