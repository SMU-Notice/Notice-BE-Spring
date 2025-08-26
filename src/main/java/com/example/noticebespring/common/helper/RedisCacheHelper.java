package com.example.noticebespring.common.helper;

import com.example.noticebespring.common.util.RedisUtil;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheHelper {

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    /**
     * 게시글 정보를 Redis에 JSON 형태로 저장
     * @param key Redis 키
     * @param post 저장할 게시글 객체
     * @param expireSeconds 만료 시간 (초 단위)
     */
    public void cachePost(String key, PostSummaryDto post, long expireSeconds) {
        try {
            String value = objectMapper.writeValueAsString(post);
            redisUtil.setDataExpire(key, value, expireSeconds);
        } catch (JsonProcessingException e) {
            log.error("Redis 저장 중 JSON 변환 실패 - key: {}", key, e);
        }
    }

    /**
     * Redis에서 게시글 정보를 가져와서 객체로 역직렬화
     * @param key Redis 키
     * @return PostSummaryDto 객체 (없으면 null)
     */
    public PostSummaryDto getPost(String key) {
        try {
            String value = redisUtil.getData(key);
            if (value == null) return null;

            return objectMapper.readValue(value, PostSummaryDto.class);
        } catch (JsonProcessingException e) {
            log.error("Redis 조회 중 JSON 역변환 실패 - key: {}", key, e);
            return null;
        }
    }
}