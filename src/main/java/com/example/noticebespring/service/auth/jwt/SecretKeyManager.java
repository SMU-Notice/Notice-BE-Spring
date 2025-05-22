package com.example.noticebespring.service.auth.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

// JWT 토큰 발급 및 검증에 쓰일 키 생성기
@Slf4j
@Component
@RequiredArgsConstructor
public class SecretKeyManager {

    @Value("${jwt.secret}")
    private String secretString;

    @Getter
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (secretString == null || secretString.length() < 64) {
            throw new IllegalStateException("JWT 시크릿 키는 최소 64바이트(Base64 인코딩 기준)여야 합니다.");
        }

        byte[] keyBytes = Base64.getDecoder().decode(secretString);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.debug("정적 JWT 비밀 키가 초기화되었습니다.");
    }

    public boolean validateToken(String token) {
        try {
            io.jsonwebtoken.Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("토큰 검증 실패", e.getMessage());
            return false;
        }
    }
}

