package com.booktalk_be.domain.category.model.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.category.model.repository.CategoryRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class CategoryRepositoryCustomImpl  extends Querydsl4RepositorySupport implements CategoryRepositoryCustom {
    protected CategoryRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }
}
