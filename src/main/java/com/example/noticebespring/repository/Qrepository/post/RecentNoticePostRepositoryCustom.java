package com.example.noticebespring.repository.Qrepository.post;

import com.example.noticebespring.dto.PostItemDto;

import java.util.List;

public interface RecentNoticePostRepositoryCustom {
    public List<PostItemDto> findRecent7Posts();
}
