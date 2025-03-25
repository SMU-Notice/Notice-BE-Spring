package com.example.noticebespring.dto.boardSubscription;

import java.util.List;

public record SubscriptionItemDto(Integer boardId, List<String> postTypes) {
}
