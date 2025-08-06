package com.example.noticebespring.common.util;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RedisUtil {
    private final StringRedisTemplate template;

    /**
     * 저장된 데이터 조회
     * @param key
     * @return
     */
    public String getData(String key) {
        ValueOperations<String, String> valueOperations = template.opsForValue();

        return valueOperations.get(key);
    }

    /**
     * 데이터 존재 여부 확인
     * @param key
     * @return
     */
    public boolean existData(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    /**
     * 데이터 저장 및 데이터 만료 기한 설정
     * @param key
     * @param value
     * @param duration
     */
    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);

        valueOperations.set(key, value, expireDuration);
    }

    /**
     * 데이터 삭제
     * @param key
     */
    public void deleteData(String key) {
        template.delete(key);
    }
}

