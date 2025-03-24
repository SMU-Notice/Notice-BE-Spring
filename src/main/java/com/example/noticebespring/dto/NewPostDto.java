package com.example.noticebespring.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record NewPostDto(
        @NotEmpty(message = "boardId cannot be empty")
        Integer boardId,
        @NotEmpty(message = "postId cannot be empty")
        List<Integer> postIds) {
}

