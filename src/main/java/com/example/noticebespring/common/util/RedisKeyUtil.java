package com.example.noticebespring.common.util;

public class RedisKeyUtil {

    private RedisKeyUtil() {
        // 생성자 private → 인스턴스 생성 방지
    }

    /**
     * 게시물 정보를 Redis에 저장하기 위한 고유 key 생성 메서드.
     *
     * @param timestamp 저장 시각 또는 기준 시간 (ex: yyyyMMddHHmmss)
     * @param boardId 게시판 ID
     * @param postId 게시물 ID
     * @return "timestamp:boardId:postId" 형식의 key 문자열
     */
    public static String generatePostKey(String timestamp, Integer boardId, Integer postId) {
        return String.format("%s:%s:%s", timestamp, boardId.toString(), postId.toString());

    }



}

