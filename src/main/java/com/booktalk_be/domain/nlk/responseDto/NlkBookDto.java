package com.booktalk_be.domain.nlk.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NlkBookDto {
    private String id;       // isbn 또는 control_no
    private String title;
    private String isbn;
    private String author;
    private String year;
    private String cover;
}
