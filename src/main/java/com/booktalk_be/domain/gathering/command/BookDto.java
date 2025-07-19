package com.booktalk_be.domain.gathering.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class BookDto {
    private String isbn;
    private String name;
    private long order;
    private String complete_yn;
    private String startDate;
}