package com.example.noticebespring.dto.email;

import com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto;

import java.util.List;

public record EmailPostContentDto(
        String email,
        String boardName,
        String campus,
        List<PostSummaryDto> postSummaryList
) {}

