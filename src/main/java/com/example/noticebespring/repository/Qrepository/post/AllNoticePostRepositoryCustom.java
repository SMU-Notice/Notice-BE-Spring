package com.example.noticebespring.repository.Qrepository.post;

import com.example.noticebespring.dto.PostItemDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AllNoticePostRepositoryCustom {
    public List<PostItemDto> findFilterPosts(Integer userId, Pageable pageable, String boardName, String postType, String searchTerm,
                                             String startDate, String endDate);
}
