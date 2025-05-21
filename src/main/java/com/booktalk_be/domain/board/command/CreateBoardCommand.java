package com.booktalk_be.domain.board.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateBoardCommand {

    @NotNull
    private Integer categoryId;
    @NotNull
    private String title;
    @NotNull
    private String content;
    private Boolean notification_yn = false;

}
