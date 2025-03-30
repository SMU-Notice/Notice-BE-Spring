package com.example.noticebespring.service.mypage;

import com.example.noticebespring.dto.bookmark.BookmarkedPostsDto;
import com.example.noticebespring.repository.BookmarkRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    // 단일 북마크 폴더에 담긴 북마크된 게시물 조회
    @Transactional(readOnly = true)
    public BookmarkedPostsDto getBookmarkedPosts(Integer userId, Integer folderId) {
        log.debug("북마크된 게시물 조회 시작 - folder: {}", folderId);

        //북마크된 게시물이 없을 경우 그냥 비어있는 상태로 반환
        BookmarkedPostsDto posts = bookmarkRepository.findAllPostsById(userId, folderId);

        if(posts == null){
            throw new EntityNotFoundException("폴더를 찾을 수 없습니다");
        }

        log.debug("북마크된 게시물 조회 성공 - folder: {}, posts: {}", folderId, posts.posts().size());
        return posts;
    }
}

