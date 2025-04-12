package com.example.noticebespring.dto;

import lombok.*;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {
    private Integer postId;          // 게시글 ID
    private String title;            // 게시글 제목
    private String contentSummary;   // 게시글 내용
    private String url;              // 원문 링크
    private Boolean hasReference;    // 첨부파일 여부
    private Boolean isBookmarked;	//북마크 여부
    private String pictureSummary;	//사진의 요약 (없으면 null)
    private Integer viewCount;		//조회수
    private LocalDate postedDate;	//게시일
}
