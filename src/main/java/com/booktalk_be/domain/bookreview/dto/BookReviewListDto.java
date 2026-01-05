package com.booktalk_be.domain.bookreview.dto;

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
    @JsonProperty("publication_date")
    private String publicationDate;
    private Integer rating;
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;
}
