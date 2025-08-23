package com.booktalk_be.domain.category.service;

import com.booktalk_be.domain.category.command.CreateCategoryCommand;
import com.booktalk_be.domain.category.responseDto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    public Integer createCategory(CreateCategoryCommand cmd);
    public List<CategoryResponse> getList();

}
