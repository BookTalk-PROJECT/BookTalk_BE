package com.booktalk_be.domain.category.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateCategoryCommand {

    @NotNull
    private String value;
    @JsonProperty("pCategoryId")
    private Integer pCategoryId;
    private Boolean isActive;
    private Integer displayOrder;

}
