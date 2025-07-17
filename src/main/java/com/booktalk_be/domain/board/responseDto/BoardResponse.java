package com.booktalk_be.domain.board.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardResponse {
    @JsonProperty("board_code")
    private String boardCode;
    private String title;
//    private final String author;
    private String date;
    private Integer views;

}
