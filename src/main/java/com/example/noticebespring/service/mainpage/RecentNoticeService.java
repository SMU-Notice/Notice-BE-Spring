package com.example.noticebespring.service.mainpage;

import com.example.noticebespring.dto.PostItemDto;
import com.example.noticebespring.repository.Qrepository.post.RecentNoticePostRepositoryCustom;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RecentNoticeService {
    private final RecentNoticePostRepositoryCustom postRepository;

    public RecentNoticeService(RecentNoticePostRepositoryCustom postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<PostItemDto> getRecentPosts(){
        log.debug("모든 공지 조회 시작 - 최근 7개 게시물");

        List<PostItemDto> posts = postRepository.findRecent7Posts();

        if (posts.isEmpty()){
            log.warn("게시물이 존재하지 않음");
            throw new EntityNotFoundException("게시판에 게시물이 존재하지 않습니다.");
        }

        log.info("모든 공지 조회 성공 - 최근 7개 게시물");
        return posts;
    }

}
