package com.booktalk_be.domain.category.service;

import com.booktalk_be.domain.category.command.CreateCategoryCommand;
import com.booktalk_be.domain.category.command.UpdateCategoryCommand;
import com.booktalk_be.domain.category.model.entity.Category;
import com.booktalk_be.domain.category.model.repository.CategoryRepository;
import com.booktalk_be.domain.category.responseDto.CategoryInfo;
import com.booktalk_be.domain.category.responseDto.CategoryResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Integer createCategory(CreateCategoryCommand cmd) {
        Category category = new Category(cmd.getValue(), cmd.getIsActive(), cmd.getPCategoryId(), cmd.getDisplayOrder());
        categoryRepository.save(category);
        return category.getCategoryId();
    }

    @Override
    public List<CategoryResponse> getList() {
        List<CategoryInfo> categoryInfos = categoryRepository.findCategories();
        return mappingCategoryTree(categoryInfos);
    }

    @Override
    public List<CategoryResponse> getAllList() {
        List<CategoryInfo> categoryInfos = categoryRepository.findAllCategories();
        return mappingCategoryTree(categoryInfos);
    }

    @Override
    public void editCategory(UpdateCategoryCommand cmd) {
        Category category = categoryRepository.findById(cmd.getCategoryId())
                .orElseThrow(EntityNotFoundException::new);
        category.edit(cmd.getValue(), cmd.getIsActive(), cmd.getDisplayOrder());
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(EntityNotFoundException::new);
        category.delete();
    }


    private List<CategoryResponse> mappingCategoryTree(List<CategoryInfo> categoryInfos) {
        List<CategoryResponse> categoryResponses = new ArrayList<>();
        Map<Integer, CategoryResponse> categoryMap = new HashMap<>();

        // 1차: 모든 카테고리 객체를 Map에 미리 생성(최상위/하위 구분 없이)
        for (CategoryInfo categoryInfo : categoryInfos) {
            CategoryResponse newCategoryResponse = CategoryResponse.builder()
                    .categoryId(categoryInfo.getCategoryId())
                    .value(categoryInfo.getValue())
                    .isActive(categoryInfo.isActive())
                    .displayOrder(categoryInfo.getDisplayOrder())
                    .build();
            categoryMap.put(categoryInfo.getCategoryId(), newCategoryResponse);
        }

        // 2차: 부모가 없는(최상위) 카테고리만 리스트에 넣고, 부모가 있으면 부모에 추가
        for (CategoryInfo categoryInfo : categoryInfos) {
            Integer pCategoryId = categoryInfo.getPCategoryId();
            CategoryResponse current = categoryMap.get(categoryInfo.getCategoryId());
            if (pCategoryId == null) {
                categoryResponses.add(current); // 최상위
            } else {
                CategoryResponse parent = categoryMap.get(pCategoryId);
                if (parent != null) parent.addSubCategory(current); // 부모에 붙이기
            }
        }
        return categoryResponses;
    }
}
