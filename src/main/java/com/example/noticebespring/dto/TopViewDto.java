package com.example.noticebespring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class TopViewDto {
    private String title;
    private LocalDate postedDate;
    private Integer viewCount;
}
