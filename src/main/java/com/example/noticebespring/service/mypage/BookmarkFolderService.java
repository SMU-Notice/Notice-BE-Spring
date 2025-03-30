package com.example.noticebespring.service.mypage;

import com.example.noticebespring.dto.bookmark.BookmarkFolderDto;
import com.example.noticebespring.entity.BookmarkFolder;
import com.example.noticebespring.repository.BookmarkFolderRepository;
import com.example.noticebespring.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.example.noticebespring.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkFolderService {
    private final BookmarkFolderRepository bookmarkFolderRepository;
    private final UserRepository userRepository;

    //새로운 북마크 폴더 생성
    @Transactional
    public BookmarkFolderDto createBookmarkFolder(Integer userId){
        log.debug("새 폴더 생성 - userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        {
                            log.error("사용자가 존재하지 않음 - userId: {}", userId);
                            return new IllegalArgumentException("사용자가 존재하지 않습니다.");
                        });

        String defaultFolderName = "새 폴더";
        String folderName = defaultFolderName;
        int folderNum = 1;

        // 폴더 이름 중복 시 새 폴더에 번호 달아서 명명
        while(bookmarkFolderRepository.existsByUserIdAndName(userId, folderName)){
            folderName = defaultFolderName + "(" + folderNum + ")";
            folderNum++;
        }

        // 북마크 폴더 생성
        BookmarkFolder folder = BookmarkFolder.builder()
                .user(user)
                .name(folderName)
                .createdAt(LocalDateTime.now())
                .build();

        BookmarkFolder newFolder = bookmarkFolderRepository.save(folder);

        log.info("새 폴더 생성 성공 - userId: {}, folderId: {}", userId, newFolder.getId());
        return new BookmarkFolderDto(newFolder.getId(),newFolder.getName(), newFolder.getCreatedAt());
    }

    // 북마크 폴더 목록 조회
    @Transactional(readOnly = true)
    public List<BookmarkFolderDto> getBookmarkFolders(Integer userId){
        log.debug("북마크 폴더 목록 조회 - userId: {}", userId);
        return bookmarkFolderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 폴더 이름 변경
    @Transactional
    public BookmarkFolderDto  updateBookmarkFolderName(Integer userId, Integer folderId, String newName){
        log.debug("폴더 이름 변경 - userId: {}, folderId: {}, newName: {}", userId, folderId, newName);
        BookmarkFolder folder = bookmarkFolderRepository.findById(folderId)
                .orElseThrow(() -> {
                    log.error("폴더가 존재하지 않음 - folderId: {}", folderId);
                    return new EntityNotFoundException("북마크 폴더가 존재하지 않습니다.");
                });

        if (!folder.getUser().getId().equals(userId)) {
            log.error("권한 없음 - userId: {}, folderId: {}", userId, folderId);
            throw new AccessDeniedException("해당 폴더에 대한 권한이 없습니다.");
        }

        if (bookmarkFolderRepository.existsByUserIdAndName(userId, newName)) {
            log.error("폴더 이름 중복 - userId: {}, newName: {}", userId, newName);
            throw new IllegalArgumentException("이미 존재하는 폴더 이름입니다.");
        }

        folder.setName(newName);
        log.info("폴더 이름 변경 성공 - userId: {}, folderId: {}, newName: {}", userId, folderId, newName);

        return new BookmarkFolderDto(folder.getId(), folder.getName(), folder.getCreatedAt());
    }

    // 폴더 삭제
    @Transactional
    public void deleteBookmarkFolder(Integer userId, Integer folderId){
        log.debug("폴더 삭제 - userId: {}, folderId: {}", userId, folderId);
        BookmarkFolder folder = bookmarkFolderRepository.findById(folderId)
                .orElseThrow(()-> {
                    log.error("폴더 존재하지 않음 - folderId: {}", folderId);
                    return new EntityNotFoundException("북마크 폴더가 존재하지 않습니다");
                });

        if(!folder.getUser().getId().equals(userId)){
            log.error("권한 없음 - userId: {}, folderId: {}", userId, folderId);
            throw new AccessDeniedException("해당 폴더에 대한 권한이 없습니다.");
        }

        bookmarkFolderRepository.deleteById(userId);
        log.info("폴더 삭제 성공 - userId: {}, folderId: {}", userId, folderId);
    }
}
