package com.example.noticebespring.service.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Queue;

// JWT 토큰 발급 및 검증을 담당
@Service
public class JwtService {
    private final SecretKeyManager secretKeyManager;

    private final JwtProperties jwtProperties;
    public JwtService(SecretKeyManager secretKeyManager, JwtProperties jwtProperties) {
        this.secretKeyManager = secretKeyManager;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(Integer userId, String email){
        SecretKey key = secretKeyManager.getCurrentKey();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token){
        SecretKey currentKey = secretKeyManager.getCurrentKey();
        Queue<SecretKey> previousKeys = secretKeyManager.getPreviousKeys();

        // 현재키로 유효성 검증
        if (isTokenValidWithKey(token, currentKey)){
            return true;
        }

        //이전키로 유효성 검증
        for (SecretKey prekey : previousKeys){
            if (isTokenValidWithKey(token, prekey)){
                return true;
            }
        }
        return false;
    }


    private boolean isTokenValidWithKey(String token, SecretKey key){
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰 반환
        }
        return null;
    }

    public Integer extractUserId(String token) {
        SecretKey key = secretKeyManager.getCurrentKey();
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.valueOf(claims.getSubject());
    }
}
