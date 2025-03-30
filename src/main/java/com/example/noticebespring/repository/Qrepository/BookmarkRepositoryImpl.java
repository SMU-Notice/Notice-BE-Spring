package com.example.noticebespring.repository.Qrepository;

import com.example.noticebespring.dto.PostItemDto;
import com.example.noticebespring.dto.bookmark.BookmarkedPostsDto;
import com.example.noticebespring.entity.QBookmark;
import com.example.noticebespring.entity.QBookmarkFolder;
import com.example.noticebespring.entity.QPost;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryImpl implements BookmarkRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QBookmarkFolder bookmarkFolder = QBookmarkFolder.bookmarkFolder;
    private final QBookmark bookmark = QBookmark.bookmark;
    private final QPost post = QPost.post;


    @Override
    public BookmarkedPostsDto findAllPostsById(Integer userId, Integer folderId) {
        //조회하고자 하는 북마크 폴더의 이름 조회
        String folderName = queryFactory
                .select(bookmarkFolder.name)
                .from(bookmarkFolder)
                .where(bookmarkFolder.id.eq(folderId)
                        .and(bookmarkFolder.user.id.eq(userId)))
                .fetchOne();

        if (folderName == null) {
            throw new IllegalArgumentException("북마크 폴더가 존재하지 않거나 권한이 없습니다.");
        }

        List<PostItemDto> posts = queryFactory
                .select(Projections.constructor(
                        PostItemDto.class,
                        post.id,
                        post.title,
                        post.viewCount,
                        post.url,
                        post.hasReference,
                        post.postedDate,
                        bookmark.id.isNotNull()
                ))
                .from(bookmark)
                .join(bookmark.bookmarkFolder, bookmarkFolder)
                .join(bookmark.post, post)
                .where(bookmark.bookmarkFolder.id.eq(folderId)
                        .and(bookmarkFolder.user.id.eq(userId))
                )
                .fetch();

        return new BookmarkedPostsDto(folderName, posts);
    }
}
