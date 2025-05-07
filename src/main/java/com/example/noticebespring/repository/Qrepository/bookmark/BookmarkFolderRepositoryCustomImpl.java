package com.example.noticebespring.repository.Qrepository.bookmark;

import com.example.noticebespring.dto.mypage.bookmark.BookmarkFolderDto;
import com.example.noticebespring.entity.QBookmarkFolder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookmarkFolderRepositoryCustomImpl implements BookmarkFolderRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBookmarkFolder bookmarkFolder = QBookmarkFolder.bookmarkFolder;

    @Override
    public List<BookmarkFolderDto> findByUserIdOrderByCreatedAtDesc(Integer userId) {
        return queryFactory
                .select(Projections.constructor(
                        BookmarkFolderDto.class,
                        bookmarkFolder.id,
                        bookmarkFolder.name,
                        bookmarkFolder.createdAt
                ))
                .from(bookmarkFolder)
                .where(bookmarkFolder.user.id.eq(userId))
                .orderBy(bookmarkFolder.createdAt.desc())
                .fetch();
    }
}
