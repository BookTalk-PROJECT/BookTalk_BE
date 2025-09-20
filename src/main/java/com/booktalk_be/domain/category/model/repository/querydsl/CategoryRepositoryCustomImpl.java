package com.booktalk_be.domain.category.model.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.category.model.repository.CategoryRepository;
import com.booktalk_be.domain.category.model.repository.CategoryRepositoryCustom;
import com.booktalk_be.domain.category.responseDto.CategoryInfo;
import com.booktalk_be.domain.category.responseDto.CategoryResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.ArrayList;
import java.util.List;

import static com.booktalk_be.domain.category.model.entity.QCategory.category;

public class CategoryRepositoryCustomImpl  extends Querydsl4RepositorySupport implements CategoryRepositoryCustom {
    protected CategoryRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }

    @Override
    public List<CategoryInfo> findCategories() {
        return select(Projections.fields(CategoryInfo.class,
                        category.categoryId,
                        category.pCategoryId,
                        category.value,
                        category.isActive))
                .from(category)
                .where(category.isActive.eq(true).and(category.delYn.eq(false)))
                .fetch();
    }

    @Override
    public List<CategoryInfo> findAllCategories() {
        return select(Projections.fields(CategoryInfo.class,
                category.categoryId,
                category.pCategoryId,
                category.value,
                category.isActive))
                .from(category)
                .where(category.delYn.eq(false))
                .fetch();
    }
}
