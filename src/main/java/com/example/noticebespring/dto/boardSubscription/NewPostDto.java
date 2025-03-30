package com.example.noticebespring.dto.boardSubscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

@Schema(description = "새로운 게시글 요청 DTO")
public record NewPostDto(

        @Schema(description = "게시판 ID", example = "1")
        int boardId,

        @Schema(
                description = "게시글 유형 (예: 카테고리별 ID 리스트)",
                example = "{ \"postTypes\": [\"학과\", \"기타\"],}"
        )
        Map<String, List<Integer>> postTypes

) {}