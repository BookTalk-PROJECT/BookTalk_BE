package com.booktalk_be.common.command;

import com.booktalk_be.common.utils.EntityEnumerable;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum KeywordType implements EntityEnumerable {
    TITLE("title", "제목"),
    WRITER("writer", "작성자");

    private final String type;
    private final String name;
}
