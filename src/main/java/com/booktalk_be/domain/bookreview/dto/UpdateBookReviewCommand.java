package com.booktalk_be.domain.bookreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UpdateBookReviewCommand {
    @Schema(description = "책 제목", example = "JPA 프로그래밍")
    private String bookTitle;
    @Schema(description = "저자", example = "김영한")
    private String authors;
    @Schema(description = "출판사", example = "에이콘")
    private String publisher;
    @Schema(description = "ISBN", example = "9788960777317")
    private String isbn;
    @Schema(description = "책 썸네일 URL", example = "http://example.com/thumbnail.jpg")
    private String thumbnail;
    @Schema(description = "서평 제목", example = "JPA 프로그래밍을 읽고")
    private String title;
    @Schema(description = "서평 내용", example = "매우 유익한 책이었습니다.")
    private String content;
    @Schema(description = "별점", example = "5")
    private Integer rating;
    @Schema(description = "공지 여부", example = "false")
    private Boolean notificationYn;
}
