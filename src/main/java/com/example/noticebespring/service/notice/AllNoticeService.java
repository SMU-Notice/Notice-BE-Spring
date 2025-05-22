package com.example.noticebespring.service.notice;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.PostItemDto;
import com.example.noticebespring.repository.Qrepository.post.AllNoticePostRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class AllNoticeService {
    private final AllNoticePostRepositoryCustom postRepository;

    public AllNoticeService(AllNoticePostRepositoryCustom postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<PostItemDto> getAllFilteredPosts(Integer userId, Pageable pageable, String boardName, String postType, String searchTerm,
                                                 String startDate, String endDate){
        log.debug("모든 공지 게시물 조회 - user: {}, boardName: {}, postType: {}, searchTerm: {}, startDate: {}, endDate: {}",
                userId, boardName, postType, searchTerm, startDate, endDate);

        try {
            List<PostItemDto> posts = postRepository.findFilterPosts(userId, pageable, boardName, postType, searchTerm, startDate, endDate);

            if(posts.isEmpty()){
                log.debug("조건을 만족하는 게시물이 없음 - user : {}, boardName : {}, postType : {}, searchTerm : {}, startDate: {}, endDate: {}",
                        userId, boardName, postType,searchTerm, startDate, endDate);
            } else{
                log.info("모든 공지 게시물 조회 성공 - user: {}, postCount: {}", userId, posts.size());
            }
            return posts;
        } catch (Exception e){
            log.error("모든 공지 게시물 조회 실패 - user: {}", userId, e);
            throw new CustomException(ErrorCode.POST_RETRIEVAL_ERROR);
        }
    }
}
