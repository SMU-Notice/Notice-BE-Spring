package com.example.noticebespring.dto.boardSubscription.postNotification;

public record UserSubscriptionInfoDto(Integer userId, String email, Integer boardId, String postType) {
}
