package com.example.noticebespring.dto.boardSubscription.postNotification;

import java.util.List;
import java.util.Map;

public record UserSubscriptionInfoGroupDto(
        Integer userId,
        String email,
        Integer boardId,
        String boardName,
        String campus,
        Map<String, List<Integer>> postTypes,
        String timestamp
) {}
