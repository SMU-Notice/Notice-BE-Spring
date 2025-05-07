package com.example.noticebespring.dto.boardSubscription.register;

import com.example.noticebespring.dto.boardSubscription.register.SubscriptionItemDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "구독 요청 DTO")
public record SubscriptionRequestDto(
        @Schema(description = "구독 항목 리스트", example = "[{\"boardId\": 1, \"postTypes\": [\"학사\",\"일반\"]}]")
        List<SubscriptionItemDto> subscriptions
) {}
