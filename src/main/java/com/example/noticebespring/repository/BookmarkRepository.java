package com.example.noticebespring.repository;

import com.example.noticebespring.entity.Bookmark;
import com.example.noticebespring.repository.Qrepository.BookmarkRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>, BookmarkRepositoryCustom {

}
