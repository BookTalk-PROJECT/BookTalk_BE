package com.booktalk_be.domain.board.command;

import com.booktalk_be.domain.board.model.entity.Board;
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

    public Board toEntity() {
        return Board.builder()
                .categoryId(this.categoryId)
                .title(this.title)
                .content(this.content)
                .notificationYn(this.notification_yn)
                .build();
    }

}
