package com.booktalk_be.domain.gathering.command;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateGatheringBoardCommand {

    @NotNull
    private String gatheringCode;

    @NotNull
    private String title;

    @NotNull
    private String content;

    private Boolean notification_yn = false;
}