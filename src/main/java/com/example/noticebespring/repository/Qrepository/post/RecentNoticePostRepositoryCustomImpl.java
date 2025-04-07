package com.example.noticebespring.repository.Qrepository.post;

import com.example.noticebespring.dto.PostItemDto;
import com.example.noticebespring.entity.Board;
import com.example.noticebespring.entity.QBoard;
import com.example.noticebespring.entity.QPost;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("recentNoticePostRepositoryCustomImpl")
public class RecentNoticePostRepositoryCustomImpl implements RecentNoticePostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;
    private final QBoard board = QBoard.board;

    public RecentNoticePostRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    //오늘 올라온 게시물인지 확인
    BooleanExpression isPostedTodayCondition = post.postedDate.eq(LocalDate.now());

    @Override
    public List<PostItemDto> findRecent7Posts() {

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
                            Expressions.asBoolean(false),
                            JPAExpressions.selectOne().from(post)
                                    .where(isPostedTodayCondition)
                                    .exists()
                    )).from(post)
                    .orderBy(post.postedDate.desc())
                    .limit(7)
                    .fetch();
            System.out.println("Fetched posts: " + posts.size());
            return posts;
        } catch (Exception e) {
            System.err.println("Error in findRecent7Posts: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
