package com.example.noticebespring.service.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

// JWT 토큰 발급 및 검증에 쓰일 키 생성기
@Slf4j
@Component
public class SecretKeyManager {

    //HS512 해싱 알고리즘을 사용하기 위함
    private static final int KEY_LENGTH_BYTES = 64;
    //최대 2개의 이전 키 유지
    private static final int MAX_PREVIOUS_KEYS = 2;

    @Getter
    private SecretKey currentKey;

    @Getter
    private final Queue<SecretKey> previousKeys = new LinkedList<>();
    // 이전 키 관리

    @PostConstruct
    public void init(){
        byte[] keyBytes = new byte[KEY_LENGTH_BYTES];
        //난수 생성기로 byte 배열을 랜덤 값으로 채움
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);
        //HMAC 비밀 키로 변환
        currentKey = Keys.hmacShaKeyFor(keyBytes);
    }

    //@Scheduled: 지정된 주기마다 자동으로 실행 (7일)
    @Scheduled(fixedRate = 604800000)
    public void rotateKey(){
        SecretKey oldKey = currentKey;
        byte[] keyBytes = new byte[KEY_LENGTH_BYTES];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);
        currentKey = Keys.hmacShaKeyFor(keyBytes);
        previousKeys.add(oldKey);

        if (previousKeys.size() > MAX_PREVIOUS_KEYS){
            previousKeys.poll();
        }
        log.info("비밀 키가 교체되었습니다: " + new Date());
    }

    public boolean validateToken(String token, SecretKey key){
        try{
            Jwts.parser().
                    verifyWith(key).
                    build().
                    parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateTokenWithRotation(String token){
        if (validateToken(token, currentKey)){
            return true;
        }

        for (SecretKey prekey : previousKeys){
            if(validateToken(token, prekey)){
                return true;
            }
        }
        return false;
    }
}

