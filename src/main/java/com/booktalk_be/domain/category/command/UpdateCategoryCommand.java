package com.booktalk_be.domain.category.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCategoryCommand {

    @NotNull
    private Integer categoryId;
    @NotBlank
    private String value;
    private Boolean isActive;
    private Integer displayOrder;

}
