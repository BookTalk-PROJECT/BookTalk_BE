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
public class BookReviewAdminListDto {
    @JsonProperty("board_code")
    private String code;
    private String title;
    private String category;
    private String author;
    private String date;
    private Boolean delYn;
    private String deleteReason;
}
