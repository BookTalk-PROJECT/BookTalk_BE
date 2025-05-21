package com.booktalk_be.domain.reply.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateReplyCommand {

    @NotNull
    private String postCode;
    @NotNull
    private String content;
    private String parentReplyCode;

}
