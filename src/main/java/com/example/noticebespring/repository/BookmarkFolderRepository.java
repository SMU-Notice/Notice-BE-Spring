package com.example.noticebespring.repository;

import com.example.noticebespring.entity.BookmarkFolder;
import com.example.noticebespring.repository.Qrepository.bookmark.BookmarkFolderRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkFolderRepository extends JpaRepository<BookmarkFolder, Integer>, BookmarkFolderRepositoryCustom {
    boolean existsByUserIdAndName(Integer userId, String name); // 폴더 이름 중복 확인
    Optional<BookmarkFolder> findByIdAndUserId(Integer postId, Integer userId); // 단일 북마크 폴더 로드

}
