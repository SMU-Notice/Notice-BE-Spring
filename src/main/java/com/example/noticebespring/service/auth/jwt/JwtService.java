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
        SecretKey key = secretKeyManager.getCurrentKey();
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
        SecretKey currentKey = secretKeyManager.getCurrentKey();
        List<SecretKey> previousKeys = secretKeyManager.getPreviousKeys();

        if (isTokenValidWithKey(token, currentKey)) {
            log.info("Token is valid with current key");
            return true;
        }

        for (SecretKey preKey : previousKeys) {
            if (isTokenValidWithKey(token, preKey)) {
                log.info("Token is valid with previous key");
                return true;
            }
        }

        log.warn("Token is not valid with any known key");
        return false;
    }

    // 특정 키로 JWT 토큰 유효성 검사
    private boolean isTokenValidWithKey(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            log.warn("JWT validation failed with key", e);
            return false;
        }
    }

    // JWT에서 사용자 ID 추출
    public Integer extractUserId(String token) {
        log.info("Extracting user ID from token");
        SecretKey currentKey = secretKeyManager.getCurrentKey();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(currentKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            log.info("Extracted subject from token: {}", subject);

            if (subject == null || !subject.matches("\\d+")) {
                log.warn("Invalid subject format in JWT: {}", subject);
                throw new CustomException(ErrorCode.JWT_TOKEN_ERROR);
            }

            return Integer.valueOf(subject);
        } catch (ExpiredJwtException e) {
            log.warn("JWT has expired", e);
            throw new CustomException(ErrorCode.JWT_TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            log.warn("JWT signature or structure error", e);
            throw new CustomException(ErrorCode.JWT_TOKEN_ERROR);
        }
    }
}

