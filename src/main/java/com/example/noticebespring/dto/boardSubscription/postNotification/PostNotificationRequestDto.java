package com.example.noticebespring.dto.boardSubscription.postNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;


public record PostNotificationRequestDto(Integer boardId, Map<String, List<Integer>> postTypes) {

    public List<String> getPostTypeNames() {
        return new ArrayList<>(postTypes.keySet());
    }

    // 모든 postType에 해당하는 postId들을 하나의 리스트로 합쳐 반환
    public List<Integer> getAllPostIds() {
        return postTypes.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Integer> getPostIdsByPostType(String postType) {
        return postTypes.getOrDefault(postType, Collections.emptyList());
    }
}
