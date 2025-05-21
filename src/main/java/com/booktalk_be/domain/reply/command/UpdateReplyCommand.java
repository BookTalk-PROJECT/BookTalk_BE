package com.booktalk_be.domain.reply.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateReplyCommand {

    @NotNull
    private String replyCode;
    @NotNull
    private String content;

}
