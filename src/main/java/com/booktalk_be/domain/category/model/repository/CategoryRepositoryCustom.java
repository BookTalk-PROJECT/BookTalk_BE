package com.booktalk_be.domain.category.model.repository;

import com.booktalk_be.domain.category.responseDto.CategoryInfo;
import com.booktalk_be.domain.category.responseDto.CategoryResponse;

import java.util.List;

public interface CategoryRepositoryCustom {

    List<CategoryInfo> findCategories();

}
