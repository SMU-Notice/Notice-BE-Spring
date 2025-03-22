package com.example.noticebespring.repository.Qrepository;

import com.example.noticebespring.dto.TopViewDto;

import java.util.List;

public interface PostRepositoryCustom {
    public List<TopViewDto> findTop7PostsByBoardName(String boardName);
}
