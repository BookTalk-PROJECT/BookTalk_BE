package com.booktalk_be.domain.bookreview.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookReviewListDto {
    private String code;
    @JsonProperty("book_title")
    private String bookTitle;
    @JsonProperty("review_title")
    private String reviewTitle;
    private String author;
    @JsonProperty("reg_date")
    private String regDate;
    private Integer rating;
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;
}
