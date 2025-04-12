package com.example.noticebespring.controller;

import com.example.noticebespring.dto.PostResponseDto;
import com.example.noticebespring.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.noticebespring.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;


@RestController
@RequestMapping("/api/main/posts")
@RequiredArgsConstructor
public class PostController {
	
    private final PostService postService;

    @Operation(summary = "상세 게시물 조회", description = "게시글 ID에 따라 상세 게시물 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostResponseDto>> getPostResponse(
            @Parameter(description = "게시글 ID", example = "82") @PathVariable("postId") Integer postId) {
        PostResponseDto dto = postService.getPostResponse(postId);
        return ResponseEntity.ok(CommonResponse.ok(dto));
    }

}

