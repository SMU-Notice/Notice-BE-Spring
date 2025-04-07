package com.example.noticebespring.repository.Qrepository.post;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.PostItemDto;
import com.example.noticebespring.entity.Board;
import com.example.noticebespring.entity.QBoard;
import com.example.noticebespring.entity.QBookmark;
import com.example.noticebespring.entity.QPost;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Repository("allNoticePostRepositoryCustomImpl")
public class AllNoticePostRepositoryCustomImpl implements AllNoticePostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;
    private final QBoard board = QBoard.board;
    private final QBookmark bookmark = QBookmark.bookmark;

    public AllNoticePostRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    //필터링을 위한 표현식

    // 게시판 이름 설정
    private BooleanExpression boardNameEquals(String name){
        return name != null ? post.board.name.eq(name) : null;
    }

    //하위 카테고리 설정
    private BooleanExpression postTypeEquals(String type){
        return type != null ? post.type.eq(type) : null;
    }

    //검색어 설정
    private BooleanExpression titleContains(String searchTerm){
        return searchTerm != null ? post.title.contains(searchTerm) : null;
    }

    //게시 날짜 설정
    private BooleanExpression postedDateBetween(String startDate, String endDate){
        if (startDate == null && endDate == null){
            return null;
        }

        if(startDate == null || endDate == null){
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE); // 시작 날짜와 끝 날짜는 필수값임
        }
        try{
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            return post.postedDate.between(start, end);
        } catch (DateTimeParseException e){
            throw new CustomException(ErrorCode.DATE_PARSING_ERROR);
        }
    }


    //필터에 따른 페이징된 게시물 반환
    @Override
    public List<PostItemDto> findFilterPosts(Integer userId, Pageable pageable, String boardName,
                                             String postType, String searchTerm, String startDate, String endDate) {

        //게시물 북마크 여부 확인
        BooleanExpression isBookmarkedCondition = bookmark.post.id.eq(post.id)
                .and(bookmark.bookmarkFolder.user.id.eq(userId));

        //오늘 올라온 게시물인지 확인
        BooleanExpression isPostedTodayCondition = post.postedDate.eq(LocalDate.now());

        try {
            List<PostItemDto> posts = queryFactory
                    .select(Projections.constructor(
                            PostItemDto.class,
                            post.id,
                            post.board.name,
                            post.title,
                            post.viewCount,
                            post.hasReference,
                            post.postedDate,
                            JPAExpressions.selectOne().from(bookmark)
                                    .where(isBookmarkedCondition)
                                    .exists(),
                            JPAExpressions.selectOne().from(post)
                                    .where(isPostedTodayCondition)
                                    .exists()
                    ))
                    .from(post)
                    .where(
                            boardNameEquals(boardName),
                            postTypeEquals(postType),
                            titleContains(searchTerm),
                            postedDateBetween(startDate, endDate)
                    )
                    .orderBy(post.postedDate.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();

            return posts;
        } catch (Exception e){
            System.err.println("Error in findRecent7Posts: " + e.getMessage());
            e.printStackTrace();
            throw new CustomException(ErrorCode.POST_RETRIEVAL_ERROR);
        }
    }
}
