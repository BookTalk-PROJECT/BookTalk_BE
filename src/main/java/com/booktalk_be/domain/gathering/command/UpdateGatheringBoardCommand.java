package com.booktalk_be.domain.gathering.command;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateGatheringBoardCommand {

    @NotNull
    private String postCode;   // == GatheringBoard.code

    private String title;
    private String content;
    private Boolean notification_yn = false;
}