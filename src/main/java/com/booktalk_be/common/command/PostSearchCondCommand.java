package com.booktalk_be.common.command;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Builder
public class PostSearchCondCommand {

    private KeywordType keywordType = KeywordType.TITLE;
    private String keyword = "";
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate = LocalDate.now();
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate = LocalDate.now();

}

