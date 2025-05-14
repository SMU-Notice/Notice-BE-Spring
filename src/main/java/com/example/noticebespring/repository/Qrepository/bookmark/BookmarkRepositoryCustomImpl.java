package com.example.noticebespring.repository.Qrepository.bookmark;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.PostItemDto;
import com.example.noticebespring.dto.mypage.bookmark.BookmarkedPostsDto;
import com.example.noticebespring.entity.QBookmark;
import com.example.noticebespring.entity.QBookmarkFolder;
import com.example.noticebespring.entity.QPost;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QBookmarkFolder bookmarkFolder = QBookmarkFolder.bookmarkFolder;
    private final QBookmark bookmark = QBookmark.bookmark;
    private final QPost post = QPost.post;


    @Override
    public BookmarkedPostsDto findAllPostsById(Integer userId, Integer folderId) {
        try {
            // 북마크 폴더 이름 조회
            String folderName = queryFactory
                    .select(bookmarkFolder.name)
                    .from(bookmarkFolder)
                    .where(bookmarkFolder.id.eq(folderId)
                            .and(bookmarkFolder.user.id.eq(userId)))
                    .fetchOne();

            if (folderName == null) {
                CustomException ex = new CustomException(ErrorCode.NOT_FOUND_FOLDER);
                log.error("북마크 폴더 없음 - userId: {}, folderId: {}", userId, folderId, ex);
                throw ex;
            }

            List<PostItemDto> posts = queryFactory
                    .select(Projections.constructor(
                            PostItemDto.class,
                            post.id,
                            Expressions.asString(""),
                            post.title,
                            post.viewCount,
                            post.hasReference,
                            post.postedDate,
                            Expressions.asBoolean(bookmark.id.isNotNull()),  // 수정된 부분
                            Expressions.asBoolean(false)
                    ))
                    .from(bookmark)
                    .join(bookmark.bookmarkFolder, bookmarkFolder)
                    .join(bookmark.post, post)
                    .where(bookmark.bookmarkFolder.id.eq(folderId)
                            .and(bookmarkFolder.user.id.eq(userId))
                    )
                    .fetch();

            return new BookmarkedPostsDto(folderName, posts);

        } catch (Exception e) {
            log.error("북마크 게시물 조회 중 예외 발생 - userId: {}, folderId: {}", userId, folderId, e);
            throw e;
        }
    }
}
