package com.example.noticebespring.service;

import com.example.noticebespring.dto.PostResponseDto;
import com.example.noticebespring.entity.Post;
import com.example.noticebespring.entity.PostPicture;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.repository.PostPictureRepository;
import com.example.noticebespring.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Service	
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostPictureRepository postPictureRepository;

    /**
     * 게시글 상세 정보를 조회
     * figma에서 보여준 제목, 북마크 여부, 첨부파일 여부, 요약된 내용, 원문 url
     */
    @Transactional(readOnly = true)
    public PostResponseDto getPostResponse(Integer postId) {
        // 게시글 조회, 없을 시 예외 처리
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        
        
        // 북마크 여부 초기는 false로 설정
        boolean isBookmarked = false;

        
        Post previous = postRepository
                .findFirstByIdLessThanOrderByIdDesc(postId)
                .orElse(null);

        Post next = postRepository
                .findFirstByIdGreaterThanOrderByIdAsc(postId)
                .orElse(null);
        
        //해당 게시글 모든 사진 조회
        List<PostPicture> pictures = postPictureRepository.findByPost_Id(postId);
        List<String> pictureSummary = null;
        
        if (!pictures.isEmpty()) {
            if (pictures.size() <= 3) {
                // 사진이 3장 이하인 경우, 각 사진의 요약 텍스트를 리스트 형태로 반환
                pictureSummary = pictures.stream()
                        .map(PostPicture::getPictureSummary)
                        .filter(s -> s != null && !s.trim().isEmpty())
                        .collect(Collectors.toList());
            } else {
                // 사진이 3장 초과 시 요약 제공 X
                pictureSummary = null;
            }
        }
        return PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .contentSummary(post.getContentSummary()) 
                .url(post.getUrl())
                .hasReference(post.getHasReference())
                .isBookmarked(isBookmarked)
                .pictureSummary(pictureSummary)
                .viewCount(post.getViewCount())
                .postedDate(post.getPostedDate())
                .previousPostId(previous != null ? previous.getId() : null)
                .previousPostTitle(previous != null ? previous.getTitle() : null)
                .nextPostId(next != null ? next.getId()    : null)
                .nextPostTitle(next != null ? next.getTitle() : null)
                .build();
    }
}
