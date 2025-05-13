package com.example.noticebespring.repository.Qrepository.post;

import com.example.noticebespring.entity.QBoard;
import com.example.noticebespring.entity.QPost;
import com.example.noticebespring.dto.main.TopViewDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("topViewPostRepositoryCustomImpl")
public class TopViewPostRepositoryCustomImpl implements TopViewPostRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;
    private final QBoard board = QBoard.board;

    private static final int RECENT_DAYS = 30;

    public TopViewPostRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    // 공통으로 적용될 쿼리에 대한 메서드
    // Projections: 쿼리 결과를 DTO로 매핑해서 반환
    private JPAQuery<TopViewDto> baseQuery(){
        return queryFactory
                .select(Projections.constructor(
                        TopViewDto.class,
                        post.id,
                        post.title,
                        post.postedDate,
                        post.viewCount))
                .from(post);
    }

    //최근 30일로 조건 설정
    private BooleanExpression postedDateInRecentDays(){
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(RECENT_DAYS);
        return post.postedDate.between(startDate, endDate);
    }

    // 게시판 이름 조건 설정
    private BooleanExpression boardNameEquals(String name){
        return name != null ? post.board.name.eq(name) : null;
    }

    @Override
    public List<TopViewDto> findTop7PostsByBoardName(String boardName) {
        return baseQuery()
                .where(postedDateInRecentDays().and(boardNameEquals(boardName)))
//                .where(boardNameEquals(boardName))
                .orderBy(post.viewCount.desc())
                .limit(7)
                .fetch();
    }
}
