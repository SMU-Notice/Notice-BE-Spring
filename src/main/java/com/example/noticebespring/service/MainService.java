package com.example.noticebespring.service;

import com.example.noticebespring.dto.TopViewDto;
import com.example.noticebespring.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
public class MainService {
    private final PostRepository postRepository;

    public MainService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<TopViewDto> getTop7PostsByBoardName(String name){
        log.debug("월간 인기 공지 조회 시작 - boardName: {}", name);

        if (name == null || name.strip().isEmpty()){
            log.warn("게시판 이름이 유효하지 않음 - boardName: {}", name);
            throw new IllegalArgumentException("게시판 이름이 유효하지 않습니다.");
        }

        List<TopViewDto> posts = postRepository.findTop7PostsByBoardName(name);

        if (posts.isEmpty()){
            log.warn("게시물이 존재하지 않음 - boardName: {}", name);
            throw new EntityNotFoundException(name + " 게시판에 게시물이 존재하지 않습니다.");
        }

        log.info("월간 인기 공지 조회 성공 - boardName: {}");

        return posts;
    }
}
