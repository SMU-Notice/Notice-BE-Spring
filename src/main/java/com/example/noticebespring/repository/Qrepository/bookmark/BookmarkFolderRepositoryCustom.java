package com.example.noticebespring.repository.Qrepository.bookmark;

import com.example.noticebespring.dto.bookmark.BookmarkFolderDto;

import java.util.List;

public interface BookmarkFolderRepositoryCustom {
    List<BookmarkFolderDto> findByUserIdOrderByCreatedAtDesc(Integer userId); // 사용자가 만든 폴더 목록 조회
}
