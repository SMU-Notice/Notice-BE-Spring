package com.example.noticebespring.service.mypage;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.mypage.bookmark.BookmarkedPostsDto;
import com.example.noticebespring.entity.Bookmark;
import com.example.noticebespring.entity.BookmarkFolder;
import com.example.noticebespring.entity.Post;
import com.example.noticebespring.repository.BookmarkFolderRepository;
import com.example.noticebespring.repository.BookmarkRepository;
import com.example.noticebespring.repository.PostRepository;
import com.example.noticebespring.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.noticebespring.entity.User;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BookmarkFolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;


    // 단일 북마크 폴더에 담긴 북마크된 게시물 조회
    @Transactional(readOnly = true)
    public BookmarkedPostsDto getBookmarkedPosts(Integer userId, Integer folderId) {
        log.debug("북마크된 게시물 조회 시작 - folder: {}", folderId);

        //북마크된 게시물이 없을 경우 그냥 비어있는 상태로 반환
        BookmarkedPostsDto posts = bookmarkRepository.findAllPostsById(userId, folderId);

        log.info("북마크된 게시물 조회 성공 - folder: {}, posts: {}", folderId, posts.posts().size());
        return posts;
    }

    //게시물에 북마크 추가
    @Transactional
    public Integer addBookmark(Integer userId, Integer folderId, Integer postId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - userId: {}", userId);
                    return new CustomException(ErrorCode.NOT_FOUND_USER);
                });
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("게시물을 찾을 수 없음 - postId: {}", postId);
                    return new CustomException(ErrorCode.NOT_FOUND_POST);
                });
        BookmarkFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> {
                    log.warn("폴더를 찾을 수 없음 - folderId: {}", folderId);
                    return new CustomException(ErrorCode.NOT_FOUND_FOLDER);
                });

        if(bookmarkRepository.existsByBookmarkFolderIdAndPostId(folderId, postId)){
            log.warn("이미 북마크된 게시물 - userId: {}, folderId: {}, postId: {}", userId, folderId, postId);
            throw new CustomException(ErrorCode.EXISTS_ALREADY_BOOKMARK);
        }

        Bookmark bookmark = Bookmark.builder()
                .bookmarkFolder(folder)
                .post(post)
                .build();
        bookmarkRepository.save(bookmark);

        log.info("북마크 추가 성공 - userId: {}, postId: {}, folderId: {}", userId, postId, folderId);

        return bookmark.getId();
    }

    //게시물에서 북마크 제거
    @Transactional
    public void removeBookmark(Integer userId, Integer folderId, Integer postId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - userId: {}", userId);
                    return new CustomException(ErrorCode.NOT_FOUND_USER);
                });
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("게시물을 찾을 수 없음 - postId: {}", postId);
                    return new CustomException(ErrorCode.NOT_FOUND_POST);
                });
        BookmarkFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> {
                    log.warn("폴더를 찾을 수 없음 - folderId: {}", folderId);
                    return new CustomException(ErrorCode.NOT_FOUND_FOLDER);
                });

        Bookmark bookmark = bookmarkRepository.findByBookmarkFolderIdAndPostId(folderId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BOOKMARK));

        bookmarkRepository.delete(bookmark);
        log.info("북마크 제거 성공 - userId: {}, folderId: {}, postId: {}", userId, folderId, postId);
    }
}

