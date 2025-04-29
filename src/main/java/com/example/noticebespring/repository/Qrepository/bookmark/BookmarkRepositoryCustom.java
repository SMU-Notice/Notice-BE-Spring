package com.example.noticebespring.repository.Qrepository.bookmark;

import com.example.noticebespring.dto.mypage.bookmark.BookmarkedPostsDto;

public interface BookmarkRepositoryCustom {
    // 북마크 폴더 내의 북마크된 게시물 조회
    BookmarkedPostsDto findAllPostsById(Integer userId, Integer folderId);
}
