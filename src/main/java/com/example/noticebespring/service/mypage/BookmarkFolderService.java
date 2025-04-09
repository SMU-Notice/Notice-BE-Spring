package com.example.noticebespring.service.mypage;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
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
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkFolderService {
    private final BookmarkFolderRepository bookmarkFolderRepository;
    private final UserRepository userRepository;

    //상명대 상징에 대한 리스트
    private static final List<String> SYMBOL_LIST = Arrays.asList(
            "스뮤", "수뭉", "상냥", "샘물", "슴우", "소목", "오스", "샘물", "자하",
            "월해", "학정", "공학", "미백", "문예", "밀레니엄"
    );

    //새로운 북마크 폴더 생성
    @Transactional
    public BookmarkFolderDto createBookmarkFolder(Integer userId){
        log.debug("새 폴더 생성 - userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        {
                            IllegalArgumentException ex = new IllegalArgumentException("사용자가 존재하지 않습니다.");
                            log.warn("사용자가 존재하지 않음 - userId: {}", userId, ex);
                            return ex;
                        });

        String folderName = null;

        //리스트에서 폴더 명명에 사용되지 않은 이름 찾기
        for(String name : SYMBOL_LIST){
            if(!bookmarkFolderRepository.existsByUserIdAndName(userId, name)){
                folderName = name;
                break;
            }
        }

        //상징들이 모두 사용되면 "새 폴더"로 명명
        if(folderName == null) {
            String defaultFolderName = "새 폴더";
            folderName = defaultFolderName;
            int folderNum = 1;

            // 폴더 이름 중복 시 '새 폴더'에 번호 추가
            while (bookmarkFolderRepository.existsByUserIdAndName(userId, folderName)) {
                folderName = defaultFolderName + "(" + folderNum + ")";
                folderNum++;
            }
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
                    EntityNotFoundException ex = new EntityNotFoundException("북마크 폴더가 존재하지 않습니다.");
                    log.warn("북마크 폴더가 존재하지 않음 - userId: {}", userId, ex);
                    throw ex;
                });

        if (!folder.getUser().getId().equals(userId)) {
            AccessDeniedException ex = new AccessDeniedException("해당 폴더에 대한 권한이 없습니다.");
            log.warn("권한 없음 - userId: {}, folderId: {}", userId, folderId, ex);
            throw ex;
        }

        if (bookmarkFolderRepository.existsByUserIdAndName(userId, newName)) {
            CustomException ex = new CustomException(ErrorCode.EXISTS_ALREADY_FOLDER_NAME);
            log.warn("폴더 이름 중복 - userId: {}, newName: {}", userId, newName, ex);
            throw ex;
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
                    EntityNotFoundException ex = new EntityNotFoundException("북마크 폴더가 존재하지 않습니다.");
                    log.warn("폴더 존재하지 않음 - folderId: {}", folderId, ex);
                    return ex;
                });

        if(!folder.getUser().getId().equals(userId)){
            AccessDeniedException ex = new AccessDeniedException("해당 폴더에 대한 권한이 없습니다.");
            log.warn("권한 없음 - userId: {}, folderId: {}", userId, folderId, ex);
            throw ex;
        }

        bookmarkFolderRepository.delete(folder);
        log.info("폴더 삭제 성공 - userId: {}, folderId: {}", userId, folderId);
    }
}
