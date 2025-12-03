package com.booktalk_be.domain.bookreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UpdateBookReviewCommand {
    @Schema(description = "서평 제목", example = "JPA 프로그래밍을 읽고(수정)")
    private String title;
    @Schema(description = "서평 내용", example = "매우 유익한 책이었습니다. 내용은 조금 어렵네요.")
    private String content;
    @Schema(description = "별점", example = "4")
    private Integer rating;
}
