package com.example.noticebespring.dto.main;

import java.time.LocalDate;

public record TopViewDto (
        Integer postId,
        String title,
        LocalDate postedDate,
        Integer viewCount
){}
