package com.example.noticebespring.dto.boardSubscription.postNotification;

import java.time.LocalDate;

public record PostSummaryDto(
        Integer boardId,
        Integer postId,
        String type,
        String title,
        String contentSummary,
        Boolean hasReference,
        String url,
        LocalDate postedDate
) {}