package com.example.noticebespring.dto.bookmark;

import java.time.LocalDateTime;

//북마크 폴더
public record BookmarkFolderDto(
        Integer id,
        String name,
        LocalDateTime createdAt
) {}
