package com.booktalk_be.domain.gathering.command;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookDto {
    private String isbn;
    private long order;
}