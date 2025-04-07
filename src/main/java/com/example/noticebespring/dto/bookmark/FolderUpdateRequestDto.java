package com.example.noticebespring.dto.bookmark;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "폴더 이름 업데이트 요청 DTO")
public record FolderUpdateRequestDto(

        @Schema(description = "변경할 폴더의 id", example = "1")
        Integer folderId,

        @Schema(description = "변경할 이름", example = "중요 공지")
        String newName
) {}
