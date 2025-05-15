package com.example.noticebespring.service.auth.jwt;

import com.example.noticebespring.common.util.RedisUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.*;

// JWT 토큰 발급 및 검증에 쓰일 키 생성기
@Slf4j
@Component
@RequiredArgsConstructor
public class SecretKeyManager {

    // HS512 해싱을 위한 키 길이 (512bit)
    private static final int KEY_LENGTH_BYTES = 64;
    private static final int MAX_PREVIOUS_KEYS = 2;
    private static final long KEY_TTL_SECONDS = 60L * 60 * 24 * 30; // 30일 (한 달)
    private static final String CURRENT_KEY = "jwt:current";
    private static final String PREVIOUS_KEY_PREFIX = "jwt:previous:";

    private final RedisUtil redisUtil;

    @Getter
    private SecretKey currentKey;

    @Getter
    private final List<SecretKey> previousKeys = new ArrayList<>();

    // 초기화 시 Redis에서 키 로드
    @PostConstruct
    public void init() {
        loadKeysFromRedis();
    }

    // Redis에서 현재 키 및 이전 키들을 로드
    private void loadKeysFromRedis() {
        try {
            String currentEncoded = redisUtil.getData(CURRENT_KEY);
            if (currentEncoded != null) {
                currentKey = decodeKey(currentEncoded);
                log.info("현재 키 로드 완료");
            } else {
                currentKey = generateAndStoreKey(CURRENT_KEY);
                log.info("새로운 현재 키 생성 및 저장 완료");
            }

            for (int i = 1; i <= MAX_PREVIOUS_KEYS; i++) {
                String encoded = redisUtil.getData(PREVIOUS_KEY_PREFIX + i);
                if (encoded != null) {
                    previousKeys.add(decodeKey(encoded));
                }
            }
            log.info("이전 키 {}개 로드 완료", previousKeys.size());

        } catch (Exception e) {
            log.error("Redis에서 키를 로드하는 데 실패했습니다.", e);
            throw new IllegalStateException("JWT 키 로딩 실패");
        }
    }

    // 한 달(30일) 주기로 키를 로테이션
    @Scheduled(fixedRate = 1000L * 60 * 60 * 24 * 30)
    public void rotateKey() {
        try {
            // 기존 currentKey → previous:1, previous:1 → previous:2
            for (int i = MAX_PREVIOUS_KEYS; i >= 2; i--) {
                String from = redisUtil.getData(PREVIOUS_KEY_PREFIX + (i - 1));
                if (from != null)
                    redisUtil.setDataExpire(PREVIOUS_KEY_PREFIX + i, from, KEY_TTL_SECONDS);
            }
            redisUtil.setDataExpire(PREVIOUS_KEY_PREFIX + 1, encodeKey(currentKey), KEY_TTL_SECONDS);

            // 새 키 생성 및 저장
            currentKey = generateAndStoreKey(CURRENT_KEY);
            log.info("키 로테이션 완료 - 새 키 저장됨");

        } catch (Exception e) {
            log.error("키 로테이션 실패", e);
        }
    }

    // 새로운 SecretKey 생성 및 Redis에 저장
    private SecretKey generateAndStoreKey(String redisKey) {
        byte[] keyBytes = new byte[KEY_LENGTH_BYTES];
        new java.security.SecureRandom().nextBytes(keyBytes);
        String encoded = Base64.getEncoder().encodeToString(keyBytes);
        redisUtil.setDataExpire(redisKey, encoded, KEY_TTL_SECONDS);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //SecretKey를 Base64 문자열로 인코딩
    private String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    //Base64 문자열을 SecretKey로 디코딩
    private SecretKey decodeKey(String encodedKey) {
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

