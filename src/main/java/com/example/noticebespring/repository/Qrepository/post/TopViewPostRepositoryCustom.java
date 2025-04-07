package com.example.noticebespring.repository.Qrepository.post;

import com.example.noticebespring.dto.main.TopViewDto;

import java.util.List;

public interface TopViewPostRepositoryCustom {
    public List<TopViewDto> findTop7PostsByBoardName(String boardName);
}
