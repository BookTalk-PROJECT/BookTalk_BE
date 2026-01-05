package com.booktalk_be.domain.bookreview.responseDto;

import com.booktalk_be.common.responseDto.PostDetailResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BookReviewDetailDto extends PostDetailResponse {
    private String code;
    @JsonProperty("book_title")
    private String bookTitle;
    private String authors;
    private String publisher;
    private String isbn;
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;
    private Integer rating;
}
