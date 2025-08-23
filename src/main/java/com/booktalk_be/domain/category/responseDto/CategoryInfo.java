package com.booktalk_be.domain.category.responseDto;

import lombok.Getter;

@Getter
public class CategoryInfo {
    private Integer categoryId;
    private Integer pCategoryId;
    private String value;
    private boolean isActive;
}