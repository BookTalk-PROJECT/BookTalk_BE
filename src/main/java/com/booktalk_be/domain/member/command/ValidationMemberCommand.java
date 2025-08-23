package com.booktalk_be.domain.member.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ValidationMemberCommand {

    @NotNull
    private String email;
}
