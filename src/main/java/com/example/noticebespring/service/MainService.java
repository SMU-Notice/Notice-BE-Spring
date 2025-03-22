package com.example.noticebespring.service;

import com.example.noticebespring.dto.TopViewDto;
import com.example.noticebespring.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MainService {
    private final PostRepository postRepository;

    public MainService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<TopViewDto> getTop7PostsByBoardName(String name){

        if (name == null || name.strip().isEmpty()){
            throw new IllegalArgumentException("게시판 이름이 유효하지 않습니다.");
        }

        List<TopViewDto> posts = postRepository.findTop7PostsByBoardName(name);

        if (posts.isEmpty()){
            throw new EntityNotFoundException(name + " 게시판에 게시물이 존재하지 않습니다.");
        }

        return posts;
    }
}
