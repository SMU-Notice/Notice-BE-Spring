package com.example.noticebespring.service.mainpage;

import com.example.noticebespring.dto.main.TopViewDto;
import com.example.noticebespring.repository.Qrepository.post.TopViewPostRepositoryCustom;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
public class TopViewService {
    private final TopViewPostRepositoryCustom postRepository;

    public TopViewService(TopViewPostRepositoryCustom postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<TopViewDto> getTop7PostsByBoardName(String name){
        log.debug("월간 인기 공지 조회 시작 - boardName: {}", name);

        if (name == null || name.strip().isEmpty()){
            IllegalArgumentException ex = new IllegalArgumentException("게시판 이름이 유효하지 않습니다.");
            log.warn("게시판 이름이 유효하지 않음 - boardName: {}", name, ex);
            throw ex;
        }

        List<TopViewDto> posts = postRepository.findTop7PostsByBoardName(name);

        if (posts.isEmpty()){
            log.warn("[WARN] 인기 공지 게시물이 없습니다 - boardName: {}", name);
            EntityNotFoundException ex = new EntityNotFoundException(name + " 게시판에 게시물이 존재하지 않습니다.");
            log.warn("게시물이 존재하지 않음 - boardName: {}", name, ex);
            throw ex;
        }

        log.info("월간 인기 공지 조회 성공 - boardName: {}");

        return posts;
    }
}
