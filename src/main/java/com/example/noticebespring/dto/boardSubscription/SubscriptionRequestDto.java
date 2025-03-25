package com.example.noticebespring.dto.boardSubscription;

import com.example.noticebespring.dto.boardSubscription.SubscriptionItemDto;
import java.util.List;

public record SubscriptionRequestDto(List<SubscriptionItemDto> subscriptions) {
}
