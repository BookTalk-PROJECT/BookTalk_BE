package com.booktalk_be.domain.board.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateBoardCommand {

    @NotNull
    private String boardCode;
    private String title;
    private String content;
    private Boolean notification_yn = false;

}
