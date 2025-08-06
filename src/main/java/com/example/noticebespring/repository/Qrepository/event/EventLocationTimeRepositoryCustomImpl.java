package com.example.noticebespring.repository.Qrepository.event;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.main.EventMapDto;
import com.example.noticebespring.entity.QBookmark;
import com.example.noticebespring.entity.QEventLocationTime;
import com.example.noticebespring.entity.QPost;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.time.LocalDate;
import java.util.List;

public class EventLocationTimeRepositoryCustomImpl implements EventLocationTimeRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;
    private final QEventLocationTime event = QEventLocationTime.eventLocationTime;
    private final QBookmark bookmark = QBookmark.bookmark;

    public EventLocationTimeRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    // 현재 날짜가 이벤트 종료 날짜보다 앞선 이벤트들만 조회
    @Override
    public List<EventMapDto> findEventsVisibleOnMap(LocalDate date, Integer userId) {

        //게시물 북마크 여부 확인
        BooleanExpression isBookmarkedCondition = bookmark.post.id.eq(post.id)
                .and(bookmark.bookmarkFolder.user.id.eq(userId));

        try {

            List<EventMapDto> events = queryFactory
                    .select(Projections.constructor(EventMapDto.class,
                            post.id,
                            event.location,
                            post.title,
                            post.contentSummary,
                            post.viewCount,
                            post.url,
                            post.postedDate,
                            JPAExpressions.selectOne().from(bookmark)
                                    .where(isBookmarkedCondition)
                                    .exists()
                    ))
                    .from(event)
                    .join(event.post, post)
                    //종료날짜가 오늘과 동일하거나 그 이후의 날짜인 이벤트만 가져오도록 필터링
                    .where(event.endDate.goe(date))
                    .orderBy(event.startDate.asc(), post.postedDate.desc())
                    .fetch();

            return events;
        }catch (Exception e){
            System.err.println("Error in findOngoingEvents: " + e.getMessage());
            throw new CustomException(ErrorCode.EVENT_RETRIEVAL_ERROR);
        }
    }
}
