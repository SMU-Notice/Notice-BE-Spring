package com.example.noticebespring.dto.boardSubscription.register;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "구독 항목 DTO")
public record SubscriptionItemDto(
        @Schema(description = "게시판 ID", example = "1")
        Integer boardId,

        @Schema(description = "게시글 유형 리스트", example = "[\"학사\", \"일반\"]")
        List<String> postTypes
) {}
