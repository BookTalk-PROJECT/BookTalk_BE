package com.booktalk_be.domain.board.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class BoardResponse {

    private final String boardCode;
    private final String title;
    private final String author;
    private final LocalDate date;
    private final Integer views;

}
