package com.example.noticebespring.service;

import com.example.noticebespring.dto.PostResponseDto;
import com.example.noticebespring.entity.Post;
import com.example.noticebespring.entity.PostPicture;
import com.example.noticebespring.repository.PostPictureRepository;
import com.example.noticebespring.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


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
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 북마크 여부 초기는 false로 설정
        boolean isBookmarked = false;
        
        //해당 게시글 모든 사진 조회
        List<PostPicture> pictures = postPictureRepository.findByPost_Id(postId);
        String pictureSummary = null;
        
        if (!pictures.isEmpty()) {
            if (pictures.size() <= 3) {
                // 사진이 3장 이하이면 모든 사진의 요약 텍스트를 연결
                pictureSummary = pictures.stream()
                        .map(PostPicture::getPictureSummary)
                        .filter(s -> s != null && !s.trim().isEmpty())
                        .reduce((s1, s2) -> s1 + "\n" + s2)
                        .orElse(null);
            } else {
                // 사진 3장 초과시 요약 제공 X 
            	 pictureSummary = "추가 이미지 있음";
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
                .build();
    }
}
