package com.example.noticebespring.repository;

import com.example.noticebespring.entity.Bookmark;
import com.example.noticebespring.repository.Qrepository.bookmark.BookmarkRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>, BookmarkRepositoryCustom {
    boolean existsByBookmarkFolderIdAndPostId(Integer folderId, Integer postId); //이미 북마크된 게시물인지 확인
    Optional<Bookmark> findByBookmarkFolderIdAndPostId(Integer folderId, Integer postId); // 북마크 조회
}
