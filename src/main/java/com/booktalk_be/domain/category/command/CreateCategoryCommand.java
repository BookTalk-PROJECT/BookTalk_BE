package com.booktalk_be.domain.category.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateCategoryCommand {

    @NotNull
    private String value;
    private Integer pCategoryId;

}
