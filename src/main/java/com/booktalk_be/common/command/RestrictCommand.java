package com.booktalk_be.common.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor
public class RestrictCommand {

    @NotNull
    private String targetCode;
    @NotNull
    private String delReason;

}
