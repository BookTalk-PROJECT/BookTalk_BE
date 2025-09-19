package com.booktalk_be.domain.category.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCategoryCommand {

    @NotNull
    private Integer categoryId;
    private String value;
    private Boolean isActive;

}
