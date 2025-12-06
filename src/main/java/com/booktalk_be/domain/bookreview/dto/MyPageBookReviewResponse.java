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
public class MyPageBookReviewResponse {
    @JsonProperty("review_id")
    private Long reviewId;
    @JsonProperty("book_title")
    private String bookTitle;
    @JsonProperty("review_title")
    private String reviewTitle;
    @JsonProperty("publication_date")
    private String publicationDate;
    private Integer rating;
}
