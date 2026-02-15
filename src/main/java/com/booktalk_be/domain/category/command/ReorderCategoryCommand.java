package com.booktalk_be.domain.category.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ReorderCategoryCommand {

    @NotNull
    @Valid
    private List<CategoryOrderItem> orders;

    @Getter
    @NoArgsConstructor
    public static class CategoryOrderItem {
        @NotNull
        private Integer categoryId;
        @NotNull
        private Integer displayOrder;
    }
}
