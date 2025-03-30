package com.example.noticebespring.dto;

import java.time.LocalDate;

public record TopViewDto (
    String title,
    LocalDate postedDate,
    Integer viewCount
){}
