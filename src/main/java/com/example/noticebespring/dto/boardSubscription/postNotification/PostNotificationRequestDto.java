package com.example.noticebespring.dto.boardSubscription.postNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;


/**
 * 게시물 알림 요청 DTO
 *
 * @param boardId 게시판 ID
 * @param postTypes 게시물 타입별 게시물 ID 리스트 매핑
 *                  - key: 게시물 타입 (예: "공지사항", "일반글", "질문글" 등)
 *                  - value: 해당 타입의 게시물 ID 리스트 (예: [1, 2, 3])
 *
 * 예시: { "공지사항": [101, 102], "일반글": [201, 202, 203] }
 */
public record PostNotificationRequestDto(Integer boardId, Map<String, List<Integer>> postTypes) {

    /**
     * 모든 게시물 타입명을 리스트로 반환
     *
     * @return 게시물 타입명 리스트 (예: ["공지사항", "일반글"])
     */
    public List<String> getPostTypeNames() {
        return new ArrayList<>(postTypes.keySet());
    }

    /**
     * 모든 게시물 타입에 해당하는 게시물 ID들을 하나의 리스트로 합쳐 반환
     *
     * @return 전체 게시물 ID 리스트 (예: [101, 102, 201, 202, 203])
     */
    public List<Integer> getAllPostIds() {
        return postTypes.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시물 타입에 해당하는 게시물 ID 리스트 반환
     *
     * @param postType 게시물 타입 (예: "공지사항")
     * @return 해당 타입의 게시물 ID 리스트, 없으면 빈 리스트 반환
     */
    public List<Integer> getPostIdsByPostType(String postType) {
        return postTypes.getOrDefault(postType, Collections.emptyList());
    }
}
