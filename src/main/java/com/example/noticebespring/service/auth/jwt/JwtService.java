package com.example.noticebespring.service.auth.jwt;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Queue;

// JWT 토큰 발급 및 검증을 담당
@Slf4j
@Service
public class JwtService {
    private final SecretKeyManager secretKeyManager;
    private final JwtProperties jwtProperties;

    public JwtService(SecretKeyManager secretKeyManager, JwtProperties jwtProperties) {
        this.secretKeyManager = secretKeyManager;
        this.jwtProperties = jwtProperties;
    }

    // JWT 토큰 생성
    public String generateToken(Integer userId, String email) {
        log.info("Generating JWT token for userId: {}, email: {}", userId, email);
        SecretKey key = secretKeyManager.getSecretKey();
        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(key)
                .compact();
        log.info("Generated token: {}", token);
        return token;
    }

    // JWT 토큰 유효성 검사 (현재 키 + 이전 키 리스트 포함)
    public boolean isTokenValid(String token) {
        log.info("Validating JWT token");
        return secretKeyManager.validateToken(token);
    }

    // JWT에서 사용자 ID 추출
    public Integer extractUserId(String token) {
        log.info("Extracting user ID from token");
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKeyManager.getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Integer.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            log.warn("JWT has expired", e);
            throw new CustomException(ErrorCode.JWT_TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            log.warn("JWT signature or structure error", e);
            throw new CustomException(ErrorCode.JWT_TOKEN_ERROR);
        }
    }
}

