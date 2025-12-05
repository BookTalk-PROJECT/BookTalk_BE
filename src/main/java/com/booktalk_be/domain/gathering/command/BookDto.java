package com.booktalk_be.domain.gathering.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {
    private String isbn;
    private String name;
    private long order;
    private String complete_yn;
    private String startDate;
}